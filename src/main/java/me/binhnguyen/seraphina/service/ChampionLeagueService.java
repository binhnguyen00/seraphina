package me.binhnguyen.seraphina.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import me.binhnguyen.seraphina.entity.League;
import me.binhnguyen.seraphina.entity.MatchDay;
import me.binhnguyen.seraphina.entity.Matchup;
import me.binhnguyen.seraphina.repository.LeagueRepo;
import me.binhnguyen.seraphina.repository.MatchDayRepo;
import me.binhnguyen.seraphina.repository.MatchupRepo;
import me.binhnguyen.seraphina.repository.TeamRepo;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class ChampionLeagueService extends LeagueService {
  public ChampionLeagueService(
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
    return "uefa.champions";
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
      League championLeague = leagueRepo.save(new League(LEAGUE_CODE, "Champion League"));
      log.info("League: {} created", championLeague.getName());
      return championLeague;
    }
  }

  @Override
  public List<MatchDay> createOrUpdateAllMatchDays() {
    log.warn("Champion League has no accurate match days");
    return Collections.emptyList();
  }

  @Override
  public List<Matchup> getCurrentWeekMatches() {
    final League league = this.get();
    final LocalDate today = LocalDate.now();
    final LocalDate THIS_MONDAY = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    final LocalDate THIS_SUNDAY = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
    final OffsetDateTime start = THIS_MONDAY.atStartOfDay().atOffset(ZoneOffset.UTC);
    final OffsetDateTime end   = THIS_SUNDAY.atTime(LocalTime.MAX).atOffset(ZoneOffset.UTC);

    try {
      return matchupRepo.getByMatchDayBetween(start, end, league.getCode());
    } catch (Exception e) {
      log.error("Failed to get {} current week matches\n{}", league.getName(), e.toString());
      return Collections.emptyList();
    }
  }
}
