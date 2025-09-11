package me.binhnguyen.seraphina.init;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.binhnguyen.seraphina.entity.League;
import me.binhnguyen.seraphina.entity.MatchDay;
import me.binhnguyen.seraphina.entity.Season;
import me.binhnguyen.seraphina.entity.Team;
import me.binhnguyen.seraphina.service.PremierLeagueService;
import me.binhnguyen.seraphina.service.SeasonService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevDataInit {
  private final SeasonService seasonService;
  private final PremierLeagueService premierLeagueService;

  @PostConstruct
  public void init() {
    initSeason();
    initLeague();
    initTeams();
    initMatchDays();
  }

  public void initSeason() {
    try {
      Season season = seasonService.createSeason();
      log.info("Season {} created", season.getYear());
    } catch (Exception e) {
      log.error("Failed to initialize seasons");
    }
  }

  private void initLeague() {
    try {
      League premierLeague = premierLeagueService.create();
      log.info("{} created", premierLeague.getName());
    } catch (Exception e) {
      log.error("Failed to initialize leagues");
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
      log.info("Premier League Match Days created. {} scheduled", matchDays.size());
    } catch (Exception e) {
      log.error("Failed to initialize match days", e);
    }
  }
}
