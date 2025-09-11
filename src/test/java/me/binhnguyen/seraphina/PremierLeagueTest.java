package me.binhnguyen.seraphina;

import lombok.extern.slf4j.Slf4j;
import me.binhnguyen.seraphina.entity.*;
import me.binhnguyen.seraphina.service.CrawlerService;
import me.binhnguyen.seraphina.service.PremierLeagueService;
import me.binhnguyen.seraphina.service.SeasonService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@SpringBootTest
class PremierLeagueTest {
  private final CrawlerService crawlerService;
  private final SeasonService seasonService;
  private final PremierLeagueService premierLeagueService;

  @Autowired
  PremierLeagueTest(
    CrawlerService crawlerService,
    SeasonService seasonService,
    PremierLeagueService premierLeagueService
  ) {
    this.crawlerService = crawlerService;
    this.seasonService = seasonService;
    this.premierLeagueService = premierLeagueService;
  }

  @BeforeEach
  void setup() {
    initSeason();
    initLeague();
    initTeams();
    initMatchDays();
  }

  private void initSeason() {
    try {
      Season season = seasonService.createSeason();
      log.info("Season {} created", season.getYear());
    } catch (Exception e) {
      log.error("Failed to initialize seasons", e);
    }
  }

  private void initLeague() {
    try {
      League premierLeague = premierLeagueService.create();
      log.info("{} created", premierLeague.getName());
    } catch (Exception e) {
      log.error("Failed to initialize leagues", e);
    }
  }

  private void initTeams() {
    try {
      List<Team> teams = premierLeagueService.createOrUpdateTeams();
      log.info("Premier League Teams created. {} teams", teams.size());
    } catch (Exception e) {
      log.error("Failed to initialize teams", e);
    }
  }

  private void initMatchDays() {
    try {
      List<MatchDay> matchDays = premierLeagueService.createOrUpdateAllMatchDays();
      log.info("Premier League MatchDays created. {} match days", matchDays.size());
    } catch (Exception e) {
      log.error("Failed to initialize match days", e);
    }
  }

  @Test
  void pullEmptyMatchesTest() {
    List<Map<String, Object>> matches = crawlerService.pullMatchesByDateRange(
        PremierLeagueService.LEAGUE_CODE,
        LocalDate.parse("2025-09-06"),
        LocalDate.parse("2025-09-07")
    );
    Assertions.assertTrue(matches.isEmpty());
  }

  @Test
  void pullMatchesTest() {
    List<Map<String, Object>> matches = crawlerService.pullMatchesByDateRange(
        PremierLeagueService.LEAGUE_CODE,
        LocalDate.parse("2025-09-13"),
        LocalDate.parse("2025-09-14")
    );
    Assertions.assertFalse(matches.isEmpty());
  }

  @Test
  void allLeagueCreateOrUpdateMatchesTest() {
    List<Matchup> matchups = premierLeagueService.createOrUpdateMatchups(
        LocalDate.parse("2025-09-13"),
        LocalDate.parse("2025-09-14")
    );
    Assertions.assertFalse(matchups.isEmpty());
    matchups.forEach(matchup -> {
      log.info("Matchup code: {}", matchup.getCode());
      log.info("Match Day: {}", matchup.getMatchDay());
    });
  }

  @Test
  void getAllMatchDayTest() {
    List<MatchDay> matchDays = premierLeagueService.getAllMatchDays();
    Assertions.assertFalse(matchDays.isEmpty());
    log.info("ALl MatchDays: {}", matchDays.size());
  }

  @Test
  void createOrUpdateAllMatchDaysTest() {
    List<MatchDay> matchDays = premierLeagueService.createOrUpdateAllMatchDays();
    log.info("All MatchDays: {}", matchDays.size());
    Assertions.assertFalse(matchDays.isEmpty());
  }

  @Test
  void getThisWeekMatchDaysTest() {
    List<MatchDay> matchDays = premierLeagueService.getThisWeekMatchDays();
    Assertions.assertFalse(matchDays.isEmpty());
    matchDays.forEach(matchDay -> log.info("Date: {}", matchDay.getDate()));
  }
}