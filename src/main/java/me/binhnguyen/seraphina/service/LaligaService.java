package me.binhnguyen.seraphina.service;

import lombok.extern.slf4j.Slf4j;
import me.binhnguyen.seraphina.repository.LeagueRepo;
import me.binhnguyen.seraphina.repository.MatchDayRepo;
import me.binhnguyen.seraphina.repository.MatchupRepo;
import me.binhnguyen.seraphina.repository.TeamRepo;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LaligaService extends LeagueService {

  public LaligaService(
    LeagueRepo leagueRepo,
    TeamRepo teamRepo,
    MatchupRepo matchupRepo,
    MatchDayRepo matchDayRepo,
    CrawlerService crawlerService,
    SeasonService seasonService
  ) {
    super(leagueRepo, teamRepo, matchupRepo, matchDayRepo, crawlerService, seasonService);
  }

  @Override
  protected String getCode() {
    return "esp.1";
  }
}
