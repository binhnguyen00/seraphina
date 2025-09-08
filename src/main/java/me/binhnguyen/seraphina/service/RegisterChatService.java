package me.binhnguyen.seraphina.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.binhnguyen.seraphina.entity.RegisterChat;
import me.binhnguyen.seraphina.repository.RegisterChatRepo;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegisterChatService {
  private final RegisterChatRepo repo;

  public RegisterChat registerChat(String lookupId, String name) {
    RegisterChat exist = repo.getByLookupId(lookupId);
    if (!Objects.isNull(exist)) {
      log.warn("Chat {} with {} already registered", lookupId, name);
      return exist;
    }
    RegisterChat record = new RegisterChat(lookupId, name);
    return repo.save(record);
  }

  public boolean unregisterChat(String lookupId) {
    RegisterChat exist = repo.getByLookupId(lookupId);
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
