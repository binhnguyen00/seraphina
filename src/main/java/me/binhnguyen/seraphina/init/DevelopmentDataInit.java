package me.binhnguyen.seraphina.init;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.binhnguyen.seraphina.entity.League;
import me.binhnguyen.seraphina.entity.Season;
import me.binhnguyen.seraphina.service.ChampionLeagueService;
import me.binhnguyen.seraphina.service.LaligaService;
import me.binhnguyen.seraphina.service.PremierLeagueService;
import me.binhnguyen.seraphina.service.SeasonService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Objects;

@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevelopmentDataInit {
  private final SeasonService seasonService;
  private final LaligaService laligaService;
  private final PremierLeagueService premierLeagueService;
  private final ChampionLeagueService championLeagueService;

  @PostConstruct
  public void init() {
    initSeason();
    initLeagues();
    initTeams();
    initMatchDays();
    initThisWeekMatchups();
  }

  private void initSeason() {
    try {
      Season season = seasonService.getCurrentSeason();
      if (Objects.isNull(season))
        seasonService.createSeason();
    } catch (Exception e) {
      log.error("Failed to initialize seasons", e);
    }
  }

  private void initLeagues() {
    try {
      League league_1 = premierLeagueService.get();
      if (Objects.isNull(league_1))
        premierLeagueService.create();
    } catch (Exception e) {
      log.error("Failed to initialize Premier League", e);
    }
    try {
      League league_2 = laligaService.get();
      if (Objects.isNull(league_2))
        laligaService.create();
    } catch (Exception e) {
      log.error("Failed to initialize Laliga", e);
    }
    try {
      League league_3 = championLeagueService.get();
      if (Objects.isNull(league_3))
        championLeagueService.create();
    } catch (Exception e) {
      log.error("Failed to initialize Champions League", e);
    }
  }

  private void initTeams() {
    try {
      premierLeagueService.createOrUpdateTeams();
    } catch (Exception e) {
      log.error("Failed to initialize Premier League teams", e);
    }
    try {
      laligaService.createOrUpdateTeams();
    } catch (Exception e) {
      log.error("Failed to initialize Laliga teams", e);
    }
    try {
      championLeagueService.createOrUpdateTeams();
    } catch (Exception e) {
      log.error("Failed to initialize Champions League teams", e);
    }
  }

  private void initMatchDays() {
    try {
      premierLeagueService.createOrUpdateAllMatchDays();
    } catch (Exception e) {
      log.error("Failed to initialize Premier League match days", e);
    }
    try {
      laligaService.createOrUpdateAllMatchDays();
    } catch (Exception e) {
      log.error("Failed to initialize Laliga match days", e);
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
