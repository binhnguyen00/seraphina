package me.binhnguyen.seraphina.service;

import lombok.extern.slf4j.Slf4j;
import me.binhnguyen.seraphina.entity.Matchup;
import me.binhnguyen.seraphina.entity.ZaloChat;
import me.binhnguyen.seraphina.repository.ZaloChatRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
public class ZaloChatService {
  private final ZaloChatRepo repo;
  private final RestTemplate restTemplate;
  private final String zaloMicroServiceUrl;

  @Autowired
  public ZaloChatService(
    ZaloChatRepo repo,
    RestTemplate restTemplate,
    @Value("${zalo.microservice.url}") String zaloMicroServiceUrl
  ) {
    this.repo = repo;
    this.restTemplate = restTemplate;
    this.zaloMicroServiceUrl = zaloMicroServiceUrl;
  }

  public ZaloChat subscribeChat(String lookupId, String name) {
    ZaloChat exist = repo.getByLookupId(lookupId);
    if (!Objects.isNull(exist)) {
      log.warn("Chat {} with {} already registered", lookupId, name);
      return exist;
    }
    ZaloChat record = new ZaloChat(lookupId, name);
    return repo.save(record);
  }

  public boolean unsubscribeChat(String lookupId) {
    ZaloChat exist = repo.getByLookupId(lookupId);
    if (Objects.isNull(exist)) {
      log.warn("Chat {} not found", lookupId);
      return false;
    }
    try {
      return repo.deleteByLookupId(exist.getLookupId());
    } catch (Exception e) {
      log.error("Failed to unregister chat {}", lookupId, e);
      return false;
    }
  }

  public List<ZaloChat> sendMessage(String message) {
    List<ZaloChat> subscribers = repo.findAll();
    String url = String.format("%s/send-message", this.zaloMicroServiceUrl);

    List<ZaloChat> failHolder = new ArrayList<>();
    List<ZaloChat> successHolder = new ArrayList<>();

    for (ZaloChat subscriber : subscribers) {
      Map<String, Object> payload = new HashMap<>();
      payload.put("chat_id", subscriber.getLookupId());
      payload.put("message", message);

      ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
        url,
        HttpMethod.POST,
        new HttpEntity<>(payload),
        new ParameterizedTypeReference<>() {}
      );

      Map<String, Object> body = response.getBody();
      if (Objects.isNull(body)) {
        failHolder.add(subscriber);
        continue;
      }

      boolean success = Objects.equals(body.get("success"), false);
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
