package me.binhnguyen.seraphina.repository;

import jakarta.persistence.Persistence;
import me.binhnguyen.seraphina.entity.Matchup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface MatchupRepo extends JpaRepository<Matchup, Persistence> {
  Matchup getByCode(String code);

  @Query("SELECT m FROM Matchup m WHERE m.matchDay BETWEEN :from AND :to ORDER BY m.matchDay ASC")
  List<Matchup> getByMatchDayBetween(OffsetDateTime from, OffsetDateTime to);

  @Query("""
    SELECT m FROM Matchup m
    WHERE
      m.matchDay BETWEEN :start AND :end
    AND
      m.league.code = :leaguecode
    ORDER BY m.matchDay ASC
  """)
  List<Matchup> getByMatchDay(OffsetDateTime start, OffsetDateTime end, String leaguecode);
}
