package me.binhnguyen.seraphina.repository;

import jakarta.persistence.Persistence;
import me.binhnguyen.seraphina.entity.MatchDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MatchDayRepo extends JpaRepository<MatchDay, Persistence> {

  @Query("SELECT md FROM MatchDay md WHERE md.season.year = :year AND md.league.code = :leagueCode ORDER BY md.date ASC")
  List<MatchDay> findByLeagueAndYear(@Param("leagueCode") String leagueCode, @Param("year") String year);

  @Query("SELECT md FROM MatchDay md WHERE md.date BETWEEN :from AND :to ORDER BY md.date ASC")
  List<MatchDay> findByDateRange(LocalDate from, LocalDate to);
}
