package me.binhnguyen.seraphina.service;

import lombok.extern.slf4j.Slf4j;
import me.binhnguyen.seraphina.entity.ZaloChat;
import me.binhnguyen.seraphina.repository.ZaloChatRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class ZaloChatService {
  private final WebClient webClient;
  private final ZaloChatRepo repo;

  @Autowired
  public ZaloChatService(
    @Qualifier("microServiceZalo") WebClient webClient,
    ZaloChatRepo repo
  ) {
    this.webClient = webClient;
    this.repo = repo;
  }

  public ZaloChat getSubscriber(String lookupId) {
    return repo.getByLookupId(lookupId);
  }

  public List<ZaloChat> getAllSubscribers() {
    return repo.findAll();
  }

  public ZaloChat subscribe(String lookupId, String name) {
    ZaloChat exist = repo.getByLookupId(lookupId);
    if (!Objects.isNull(exist)) {
      log.warn("Chat {} with {} already registered", lookupId, name);
      return exist;
    }
    ZaloChat record = new ZaloChat(lookupId, name);
    return repo.save(record);
  }

  public boolean unsubscribe(String lookupId) {
    ZaloChat exist = repo.getByLookupId(lookupId);
    if (Objects.isNull(exist)) {
      log.warn("Chat {} not found", lookupId);
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

  @SuppressWarnings("unchecked")
  public List<ZaloChat> sendMessageTo(List<ZaloChat> subscribers, String message) {
    List<ZaloChat> failHolder = new ArrayList<>();
    List<ZaloChat> successHolder = new ArrayList<>();

    for (ZaloChat subscriber : subscribers) {
      Map<String, Object> response = this.webClient.post()
        .uri(uriBuilder -> uriBuilder
          .path("/send-message")
          .queryParam("chat_id", subscriber.getLookupId())
          .queryParam("message", message)
          .build())
        .retrieve()
        .bodyToMono(Map.class)
        .block();

      if (Objects.isNull(response)) {
        log.error("sendMessageTo API has no response");
        failHolder.add(subscriber);
        continue;
      }

      boolean success = Objects.equals(response.get("success"), false);
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
