package me.binhnguyen.seraphina.repository;

import jakarta.persistence.Persistence;
import me.binhnguyen.seraphina.entity.ZaloChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ZaloChatRepo extends JpaRepository<ZaloChat, Persistence> {
  ZaloChat getByLookupId(String lookupId);

  @Modifying
  @Query("DELETE FROM ZaloChat WHERE lookupId = :lookupId")
  int deleteByLookupId(String lookupId);
}
