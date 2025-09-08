package me.binhnguyen.seraphina.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.binhnguyen.seraphina.entity.ZaloChat;
import me.binhnguyen.seraphina.repository.ZaloChatRepo;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class ZaloChatService {
  private final ZaloChatRepo repo;

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
}
