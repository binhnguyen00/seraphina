package me.binhnguyen.seraphina;

import lombok.extern.slf4j.Slf4j;
import me.binhnguyen.seraphina.entity.Season;
import me.binhnguyen.seraphina.service.LaligaService;
import me.binhnguyen.seraphina.service.PremierLeagueService;
import me.binhnguyen.seraphina.service.SeasonService;
import me.binhnguyen.seraphina.service.SubscriberService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

@Slf4j
public abstract class InitDataTest {

  @Autowired
  public SeasonService seasonService;

  @Autowired
  public PremierLeagueService premierLeagueService;

  @Autowired
  public LaligaService laligaService;

  @Autowired
  public SubscriberService subscriberService;

  @BeforeEach
  void setup() {
    initSeason();
    initLeague();
    initTeams();
    initMatchDays();
    initThisWeekMatchups();
    initSubscribers();
  }

  private void initSubscribers() {
    try {
      subscriberService.subscribe("306e6075fc20157e4c31", "Binh Nguyen");
    } catch (Exception e) {
      log.error("Failed to initialize subscribers", e);
    }
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
      premierLeagueService.create();
      laligaService.create();
    } catch (Exception e) {
      log.error("Failed to initialize leagues", e);
    }
  }

  private void initTeams() {
    try {
      premierLeagueService.createOrUpdateTeams();
      laligaService.createOrUpdateTeams();
    } catch (Exception e) {
      log.error("Failed to initialize teams", e);
    }
  }

  private void initMatchDays() {
    try {
      premierLeagueService.createOrUpdateAllMatchDays();
      laligaService.createOrUpdateAllMatchDays();
    } catch (Exception e) {
      log.error("Failed to initialize match days", e);
    }
  }

  private void initThisWeekMatchups() {
    final LocalDate today = LocalDate.now();
    final LocalDate THIS_MONDAY = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    final LocalDate THIS_SUNDAY = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

    try {
      premierLeagueService.createOrUpdateMatchups(THIS_MONDAY, THIS_SUNDAY);
      laligaService.createOrUpdateMatchups(THIS_MONDAY, THIS_SUNDAY);
    } catch (Exception e) {
      log.error("Failed to initialize this week matchups", e);
    }
  }
}
