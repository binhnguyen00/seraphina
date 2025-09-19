package me.binhnguyen.seraphina.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import me.binhnguyen.seraphina.entity.League;
import me.binhnguyen.seraphina.entity.Subscriber;
import me.binhnguyen.seraphina.repository.SubscriberRepo;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class SubscriberService {
  private final WebClient webClient;
  private final SubscriberRepo repo;
  private final LaligaService laligaService;
  private final PremierLeagueService premierLeagueService;
  private final ChampionLeagueService championLeagueService;

  public record ServiceResult(boolean success, String message, Subscriber subscriber) {}

  public SubscriberService(
    @Qualifier("microServiceZalo") WebClient webClient,
    SubscriberRepo repo,
    LaligaService laligaService,
    PremierLeagueService premierLeagueService,
    ChampionLeagueService championLeagueService
  ) {
    this.webClient = webClient;
    this.repo = repo;
    this.laligaService = laligaService;
    this.premierLeagueService = premierLeagueService;
    this.championLeagueService = championLeagueService;
  }

  @Transactional
  public Subscriber getSubscriber(String lookupId) {
    Subscriber subscriber = repo.getByLookupId(lookupId);
    // pre-fetch following leagues. avoid "could not initialize proxy - no Session" error
    if (Objects.nonNull(subscriber)) subscriber.getFollowingLeagues().size();
    return subscriber;
  }

  public List<Subscriber> getAllSubscribers() {
    return repo.findAll();
  }

  @Transactional
  public ServiceResult subscribe(String lookupId, String name) {
    Subscriber exist = repo.getByLookupId(lookupId);
    if (Objects.nonNull(exist)) {
      log.warn("Subscriber {} with name {} already registered", lookupId, name);
      return new ServiceResult(
        false,
        String.format("Người dùng %s đã đăng ký", exist.getName()),
        exist
      );
    }
    League premierLeague = premierLeagueService.get();
    Subscriber record = repo.save(new Subscriber(lookupId, name));
    this.followLeague(record.getLookupId(), premierLeague.getCode());
    return new ServiceResult(
      true,
      "Đăng ký thành công!",
      record
    );
  }

  @Transactional
  public boolean unsubscribe(String lookupId) {
    Subscriber exist = repo.getByLookupId(lookupId);
    if (Objects.isNull(exist)) {
      log.warn("Can't unsubscribe! Subscriber is {} not found", lookupId);
      return false;
    }
    try {
      int numb = repo.deleteByLookupId(exist.getLookupId());
      return numb > 0;
    } catch (Exception e) {
      log.error("Failed to unregister chat {}", lookupId, e);
      return false;
    }
  }

  /**
   * The method checks if the subscriber exists and if they are not already following
   * the specified league. If all validations pass, the league is added to the
   * subscriber's followed leagues.
   *
   * @param lookupId the unique identifier of the subscriber
   * @param leagueCode the code of the league to follow (e.g., "eng.1" for Premier League, "esp.1" for LaLiga)
   * @return ServiceResult containing the operation status, a message, and the updated list of followed leagues
   * @see ServiceResult
   */
  @Transactional
  public ServiceResult followLeague(String lookupId, String leagueCode) {
    Subscriber exist = repo.getByLookupId(lookupId);
    if (Objects.isNull(exist)) {
      log.warn("Can't follow league! Subscriber is {} not found", lookupId);
      return new ServiceResult(
        false,
        "Bạn chưa đăng ký",
        null
      );
    }
    League league = switch (leagueCode) {
      case "eng.1" -> premierLeagueService.get();
      case "esp.1" -> laligaService.get();
      case "uefa.champions" -> championLeagueService.get();
      default -> null;
    };
    if (Objects.isNull(league))
      return new ServiceResult(
        false,
        "Theo dõi giải đấu thất bại",
        null
      );

    if (exist.getFollowingLeagues().contains(league)) {
      log.warn("Subscriber {} is already following league {}", lookupId, leagueCode);
      return new ServiceResult(
        false,
        "Bạn đang theo dõi giải đấu này rồi",
        null
      );
    }

    exist.getFollowingLeagues().add(league);
    return new ServiceResult(
      true,
      String.format("Theo dõi giải đấu %s thành công", league.getName()),
      null
    );
  }

  @Transactional
  public ServiceResult unfollowLeague(String lookupId, String leagueCode) {
    Subscriber exist = repo.getByLookupId(lookupId);
    if (Objects.isNull(exist)) {
      log.warn("Can't unfollow league! Subscriber is {} not found", lookupId);
      return new ServiceResult(
        false,
        "Bạn chưa đăng ký",
        null
      );
    }
    League league = switch (leagueCode) {
      case "eng.1" -> premierLeagueService.get();
      case "esp.1" -> laligaService.get();
      case "uefa.champions" -> championLeagueService.get();
      default -> null;
    };
    if (Objects.isNull(league))
      return new ServiceResult(
        false,
        "Hủy theo dõi giải đấu thất bại",
        null
      );

    exist.getFollowingLeagues().remove(league);
    return new ServiceResult(
      true,
      String.format("Hủy theo dõi giải %s", league.getName()),
      null
    );
  }

  public List<Subscriber> sendMessageTo(List<Subscriber> subscribers, String message) {
    List<Subscriber> failHolder = new ArrayList<>();
    List<Subscriber> successHolder = new ArrayList<>();

    for (Subscriber subscriber : subscribers) {
      Map<String, Object> response = this.webClient.post()
        .uri(uriBuilder -> uriBuilder
          .path("/send-message")
          .queryParam("user_id", subscriber.getLookupId())
          .queryParam("message", message)
          .build())
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
        .block();

      if (Objects.isNull(response)) {
        log.error("sendMessageTo API has no response");
        failHolder.add(subscriber);
        continue;
      }

      boolean success = Boolean.TRUE.equals(response.get("success"));
      if (success) {
        successHolder.add(subscriber);
      } else {
        failHolder.add(subscriber);
      }
    }

    successHolder.forEach(success -> log.info("Sent message to chat {}, {}", success.getLookupId(), success.getName()));
    failHolder.forEach(fail -> log.error("Failed to send message to chat {}, {}", fail.getLookupId(), fail.getName()));

    return successHolder;
  }
}
