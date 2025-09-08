package me.binhnguyen.seraphina.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

  public void sendMessage(String message) {
    List<ZaloChat> chats = repo.findAll();

    String url = String.format("%s/send-message", this.zaloMicroServiceUrl);

    chats.forEach(chat -> {
      Map<String, Object> payload = new HashMap<>();
      payload.put("chat_id", chat.getLookupId());
      payload.put("message", message);
      ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
        url,
        HttpMethod.POST,
        new HttpEntity<>(payload),
        new ParameterizedTypeReference<>() {}
      );
    });
  }
}
