package me.binhnguyen.seraphina.repository;

import jakarta.persistence.Persistence;
import me.binhnguyen.seraphina.entity.Subscriber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface SubscriberRepo extends JpaRepository<Subscriber, Persistence> {
  Subscriber getByLookupId(String lookupId);

  @Modifying
  @Query("DELETE FROM Subscriber WHERE lookupId = :lookupId")
  int deleteByLookupId(String lookupId);
}
