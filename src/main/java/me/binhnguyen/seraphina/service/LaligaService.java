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

  @Override
  @Transactional
  public League create() {
    final String LEAGUE_CODE = this.getCode();
    League exist = leagueRepo.getByCode(LEAGUE_CODE);
    if (Objects.nonNull(exist)) {
      log.warn("League: {} is already exist", exist.getName());
      return exist;
    } else {
      League laliga = leagueRepo.save(new League(LEAGUE_CODE, "Laliga"));
      log.info("League: {} created", laliga.getName());
      return laliga;
    }
  }
}
