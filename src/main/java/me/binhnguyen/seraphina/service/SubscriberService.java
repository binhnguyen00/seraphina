package me.binhnguyen.seraphina.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import me.binhnguyen.seraphina.entity.League;
import me.binhnguyen.seraphina.entity.Subscriber;
import me.binhnguyen.seraphina.repository.SubscriberRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@Slf4j
@Service
public class SubscriberService {
  private final WebClient webClient;
  private final SubscriberRepo repo;
  private final LaligaService laligaService;
  private final PremierLeagueService premierLeagueService;

  public record SubscribeResult(Subscriber subscriber, boolean isNew) {}

  @Autowired
  public SubscriberService(
    @Qualifier("microServiceZalo") WebClient webClient,
    SubscriberRepo repo,
    LaligaService laligaService,
    PremierLeagueService premierLeagueService
  ) {
    this.webClient = webClient;
    this.repo = repo;
    this.laligaService = laligaService;
    this.premierLeagueService = premierLeagueService;
  }

  public Subscriber getSubscriber(String lookupId) {
    return repo.getByLookupId(lookupId);
  }

  public List<Subscriber> getAllSubscribers() {
    return repo.findAll();
  }

  @Transactional
  public SubscribeResult subscribe(String lookupId, String name) {
    Subscriber exist = repo.getByLookupId(lookupId);
    if (!Objects.isNull(exist)) {
      log.warn("Chat {} with {} already registered", lookupId, name);
      return new SubscribeResult(exist, false);
    }
    Subscriber record = repo.save(new Subscriber(lookupId, name));
    return new SubscribeResult(record,true);
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

  @Transactional
  public List<League> follow(String lookupId, String leagueCode) {
    Subscriber exist = repo.getByLookupId(lookupId);
    if (Objects.isNull(exist)) {
      log.warn("Can't follow league! Subscriber is {} not found", lookupId);
      return Collections.emptyList();
    }
    League league = switch (leagueCode) {
      case "eng.1" -> premierLeagueService.get();
      case "esp.1" -> laligaService.get();
      default -> null;
    };
    if (Objects.isNull(league))
      return exist.getFollowingLeagues();

    if (exist.getFollowingLeagues().contains(league)) {
      log.warn("Subscriber {} is already following league {}", lookupId, leagueCode);
      return exist.getFollowingLeagues();
    }

    exist.getFollowingLeagues().add(league);
    return exist.getFollowingLeagues();
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
