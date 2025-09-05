package me.binhnguyen.seraphina.repository;

import jakarta.persistence.Persistence;
import me.binhnguyen.seraphina.entity.Season;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchupRepo extends JpaRepository<Season, Persistence> {
}
