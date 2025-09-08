package me.binhnguyen.seraphina.repository;

import jakarta.persistence.Persistence;
import me.binhnguyen.seraphina.entity.RegisterChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RegisterChatRepo extends JpaRepository<RegisterChat, Persistence> {
  RegisterChat getByLookupId(String lookupId);

  @Query("DELETE FROM RegisterChat WHERE lookupId = :lookupId")
  boolean deleteByLookupId(String lookupId);
}
