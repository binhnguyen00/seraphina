package me.binhnguyen.seraphina;

import lombok.extern.slf4j.Slf4j;
import me.binhnguyen.seraphina.entity.League;
import me.binhnguyen.seraphina.entity.MatchDay;
import me.binhnguyen.seraphina.entity.Season;
import me.binhnguyen.seraphina.entity.Team;
import me.binhnguyen.seraphina.service.CrawlerService;
import me.binhnguyen.seraphina.service.PremierLeagueService;
import me.binhnguyen.seraphina.service.SeasonService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Slf4j
public abstract class InitDataTest {

  @Autowired
  public CrawlerService crawlerService;

  @Autowired
  public SeasonService seasonService;

  @Autowired
  public PremierLeagueService premierLeagueService;

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
}
