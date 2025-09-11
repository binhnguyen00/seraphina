package me.binhnguyen.seraphina;

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

@SpringBootTest
class ServicesTests {
  @Autowired
  private CrawlerService crawlerService;

  @Autowired
  private SeasonService seasonService;

  @Autowired
  private PremierLeagueService premierLeagueService;

  @BeforeEach
  void setup() {
    initSeason();
    initLeague();
    initTeams();
  }

  private void initSeason() {
    try {
      Season season = seasonService.createSeason();
      System.out.printf("Season %s created\n", season.getYear());
    } catch (Exception e) {
      System.out.printf("Failed to initialize seasons %s\n", e);
    }
  }

  private void initLeague() {
    try {
      League premierLeague = premierLeagueService.create();
      System.out.printf("%s created\n", premierLeague.getName());
    } catch (Exception e) {
      System.out.printf("Failed to initialize leagues \n%s", e);
    }
  }

  private void initTeams() {
    try {
      List<Team> teams = premierLeagueService.createOrUpdateTeams();
      System.out.printf("Premier League Teams created. %s teams%n", teams.size());
    } catch (Exception e) {
      System.out.printf("Failed to initialize teams %s\n", e);
    }
  }

  @Test
  void pullEmptyMatchesTest() {
    List<Map<String, Object>> matches = crawlerService.pullMatchesByDateRange(
      PremierLeagueService.LEAGUE_ID,
      LocalDate.parse("2025-09-06"),
      LocalDate.parse("2025-09-07")
    );
    Assertions.assertTrue(matches.isEmpty());
  }

  @Test
  void pullMatchesTest() {
    List<Map<String, Object>> matches = crawlerService.pullMatchesByDateRange(
      PremierLeagueService.LEAGUE_ID,
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
      System.out.println(matchup.getCode());
      System.out.println(matchup.getMatchDay().toString());
    });
  }

  @Test
  void getAllMatchDayTest() {
    List<MatchDay> matchDays = premierLeagueService.getAllMatchDays();
    Assertions.assertFalse(matchDays.isEmpty());
    matchDays.forEach(matchDay -> {
      System.out.println(matchDay.getDate());
    });
  }

  @Test
  void createOrUpdateAllMatchDaysTest() {
    List<MatchDay> matchDays = premierLeagueService.createOrUpdateAllMatchDays();
    Assertions.assertFalse(matchDays.isEmpty());
    matchDays.forEach(matchDay -> System.out.println(matchDay.getDate()));
  }
}
