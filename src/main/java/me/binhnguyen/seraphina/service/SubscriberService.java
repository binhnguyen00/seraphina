package me.binhnguyen.seraphina.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import me.binhnguyen.seraphina.common.DataRecord;
import me.binhnguyen.seraphina.entity.League;
import me.binhnguyen.seraphina.entity.Matchup;
import me.binhnguyen.seraphina.entity.Subscriber;
import me.binhnguyen.seraphina.repository.SubscriberRepo;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.*;

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

  public boolean sendMessageTo(Subscriber subscriber, List<DataRecord> leagues) {
    Map<String, Object> response = this.webClient.post()
      .uri(uriBuilder -> uriBuilder
        .path("/send-message")
        .queryParam("user_id", subscriber.getLookupId())
        .queryParam("leagues", leagues)
        .build())
      .retrieve()
      .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
      .block();

    if (Objects.isNull(response)) {
      log.error("sendMessageTo API has no response");
      return false;
    }

    boolean success = Boolean.TRUE.equals(response.get("success"));
    return !success;
  }

  /**
   * Retrieves the list of leagues and their upcoming matches that the subscriber is following.
   * The matches are filtered to show only those scheduled for the next day.
   *
   * @param subscriber The subscriber whose followed leagues and matches to retrieve
   * @return A list of DataRecord objects, where each record represents a league and contains: <br/>
   *         <code>league</code>: League name <br/>
   *         <code>matches</code>: List of Matchup objects for the next day's matches in that league <br/>
   *         Returns an empty list if the subscriber is not following any leagues.
   */
  public List<DataRecord> getLeagues(Subscriber subscriber, LocalDate from, LocalDate to) {
    if (subscriber.getFollowingLeagues().isEmpty())
      return Collections.emptyList();

    List<DataRecord> leagues = new ArrayList<>();
    for (League league : subscriber.getFollowingLeagues()) {
      List<Matchup> matchups = switch (league.getCode()) {
        case "eng.1" -> premierLeagueService.getMatchesByDateRange(from, to);
        case "esp.1" -> laligaService.getMatchesByDateRange(from, to);
        case "uefa.champions" -> championLeagueService.getMatchesByDateRange(from, to);
        default -> Collections.emptyList();
      };
      if (matchups.isEmpty()) continue;
      leagues.add(DataRecord.spawn()
        .with("name", league.getName())
        .with("matches", matchups.stream().map(Matchup::toDataRecord).toList())
      );
    }

    return leagues;
  }
}
