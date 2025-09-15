package me.binhnguyen.seraphina.init;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.binhnguyen.seraphina.repository.LeagueRepo;
import me.binhnguyen.seraphina.service.LaligaService;
import me.binhnguyen.seraphina.service.PremierLeagueService;
import me.binhnguyen.seraphina.service.SeasonService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevDataInit {
  private final LeagueRepo leagueRepo;
  private final SeasonService seasonService;
  private final LaligaService laligaService;
  private final PremierLeagueService premierLeagueService;

  @PostConstruct
  public void init() {
    initSeason();
    initLeague();
    initTeams();
    initMatchDays();
    initThisWeekMatchups();
  }

  private void initSeason() {
    try {
      seasonService.createSeason();
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
