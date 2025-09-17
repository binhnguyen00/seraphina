package me.binhnguyen.seraphina;

import lombok.extern.slf4j.Slf4j;
import me.binhnguyen.seraphina.entity.League;
import me.binhnguyen.seraphina.entity.MatchDay;
import me.binhnguyen.seraphina.entity.Season;
import me.binhnguyen.seraphina.entity.Team;
import me.binhnguyen.seraphina.service.CrawlerService;
import me.binhnguyen.seraphina.service.PremierLeagueService;
import me.binhnguyen.seraphina.service.SeasonService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
public class CrawlerTest {

  @Autowired
  private CrawlerService crawlerService;

  @Autowired
  public SeasonService seasonService;

  @Autowired
  private PremierLeagueService premierLeagueService;

  @BeforeEach
  void setup() {
    initSeason();
    initLeague();
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

  @Test
  void pullMatchesTest_1() {
    LocalDate today = LocalDate.now();
    LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    LocalDate sunday = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

    List<Map<String, Object>> matches = crawlerService.pullMatchesByDateRange(premierLeagueService.getCode(), monday, sunday);
    log.info("Found {} matches in {} - {}", matches.size(), monday, sunday);
    Assertions.assertFalse(matches.isEmpty());
  }

  @Test
  void pullMatchesTest_2() {
    premierLeagueService.createOrUpdateAllMatchDays();

    List<MatchDay> matchDays = premierLeagueService.getThisWeekMatchDays();
    List<Map<String, Object>> matches = crawlerService.pullMatchesByDateRange(
      premierLeagueService.getCode(),
      matchDays.getFirst().getDate(),
      matchDays.getLast().getDate()
    );
    log.info("Found {} matches", matches.size());
    Assertions.assertFalse(matches.isEmpty());
  }

  @Test
  void pullTeamTest() {
    List<Team> teams = crawlerService.pullTeams(premierLeagueService.getCode());
    log.info("Found {} teams", teams.size());
    Assertions.assertFalse(teams.isEmpty());
  }

  @Test
  void pullCurrentSeasonScheduleMatchDaysTest() {
    List<LocalDate> matchDays = crawlerService.pullCurrentSeasonScheduleMatchDays(premierLeagueService.getCode());
    log.info("Found {} match days", matchDays.size());
    Assertions.assertFalse(matchDays.isEmpty());
  }
}
