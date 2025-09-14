package me.binhnguyen.seraphina.service;


import lombok.extern.slf4j.Slf4j;
import me.binhnguyen.seraphina.repository.LeagueRepo;
import me.binhnguyen.seraphina.repository.MatchDayRepo;
import me.binhnguyen.seraphina.repository.MatchupRepo;
import me.binhnguyen.seraphina.repository.TeamRepo;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PremierLeagueService extends LeagueService {
  public PremierLeagueService(
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
  public String getCode() {
    return "eng.1";
  }
}
