package me.binhnguyen.seraphina.repository;

import jakarta.persistence.Persistence;
import me.binhnguyen.seraphina.entity.MatchDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MatchDayRepo extends JpaRepository<MatchDay, Persistence> {

  @Query("SELECT md FROM MatchDay md WHERE md.season.year = :year AND md.league.id = :leagueId ORDER BY md.date ASC")
  List<MatchDay> getMatchDay(String leagueId, int year);
}
