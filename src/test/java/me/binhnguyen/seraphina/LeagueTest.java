package me.binhnguyen.seraphina;

import lombok.extern.slf4j.Slf4j;
import me.binhnguyen.seraphina.entity.MatchDay;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@Slf4j
@SpringBootTest
class LeagueTest extends InitDataTest {

  // ==========================================
  // Premier League
  // ==========================================

  @Test
  void getPremierLeagueAllMatchDayTest() {
    List<MatchDay> matchDays = premierLeagueService.getAllMatchDays();
    Assertions.assertFalse(matchDays.isEmpty());
    log.info("getPremierLeagueAllMatchDayTest\nPremier League all MatchDays: {}", matchDays.size());
  }

  @Test
  void getPremierLeagueThisWeekMatchDaysTest() {
    List<MatchDay> matchDays = premierLeagueService.getThisWeekMatchDays();
    Assertions.assertFalse(matchDays.isEmpty());
    matchDays.forEach(matchDay -> log.info("getPremierLeagueThisWeekMatchDaysTest\nDate: {}", matchDay.getDate()));
  }

  @Test
  void createOrUpdateAllPremierLeagueMatchDaysTest() {
    List<MatchDay> matchDays = premierLeagueService.createOrUpdateAllMatchDays();
    log.info("createOrUpdateAllPremierLeagueMatchDaysTest\nPremier League all MatchDays: {}", matchDays.size());
    Assertions.assertFalse(matchDays.isEmpty());
  }

  // ==========================================
  // Laliga
  // ==========================================

  @Test
  void getLaligaAllMatchDayTest() {
    List<MatchDay> matchDays = laligaService.getAllMatchDays();
    Assertions.assertFalse(matchDays.isEmpty());
    log.info("getLaligaAllMatchDayTest\nLaliga all MatchDays: {}", matchDays.size());
  }

  @Test
  void getLaligaThisWeekMatchDaysTest() {
    List<MatchDay> matchDays = laligaService.getThisWeekMatchDays();
    Assertions.assertFalse(matchDays.isEmpty());
    matchDays.forEach(matchDay -> log.info("getLaligaThisWeekMatchDaysTest\nDate: {}", matchDay.getDate()));
  }

  @Test
  void createOrUpdateAllLaligaMatchDaysTest() {
    List<MatchDay> matchDays = laligaService.createOrUpdateAllMatchDays();
    log.info("createOrUpdateAllLaligaMatchDaysTest\nLaliga all MatchDays: {}", matchDays.size());
    Assertions.assertFalse(matchDays.isEmpty());
  }
}