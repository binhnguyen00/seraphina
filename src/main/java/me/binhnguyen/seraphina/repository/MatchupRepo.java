package me.binhnguyen.seraphina.repository;

import jakarta.persistence.Persistence;
import me.binhnguyen.seraphina.entity.Matchup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MatchupRepo extends JpaRepository<Matchup, Persistence> {
  Matchup getByCode(String code);

  @Query("SELECT m FROM Matchup m WHERE m.matchDay BETWEEN :from AND :to")
  List<Matchup> getByMatchDayBetween(LocalDate from, LocalDate to);
}
