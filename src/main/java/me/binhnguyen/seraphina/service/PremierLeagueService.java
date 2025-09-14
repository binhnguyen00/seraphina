package me.binhnguyen.seraphina.service;


import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import me.binhnguyen.seraphina.entity.League;
import me.binhnguyen.seraphina.repository.LeagueRepo;
import me.binhnguyen.seraphina.repository.MatchDayRepo;
import me.binhnguyen.seraphina.repository.MatchupRepo;
import me.binhnguyen.seraphina.repository.TeamRepo;
import org.springframework.stereotype.Service;

import java.util.Objects;

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

  @Override
  @Transactional
  public League create() {
    final String LEAGUE_CODE = this.getCode();
    League exist = leagueRepo.getByCode(LEAGUE_CODE);
    if (Objects.nonNull(exist)) {
      log.warn("League: {} is already exist", exist.getName());
      return exist;
    } else {
      League premierLeague = leagueRepo.save(new League(LEAGUE_CODE, "Premier League"));
      log.info("League: {} created", premierLeague.getName());
      return premierLeague;
    }
  }
}
