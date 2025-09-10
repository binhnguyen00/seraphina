package me.binhnguyen.seraphina.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.binhnguyen.seraphina.entity.League;
import me.binhnguyen.seraphina.entity.Season;
import me.binhnguyen.seraphina.repository.LeagueRepo;
import me.binhnguyen.seraphina.repository.SeasonRepo;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeasonService {
  private final SeasonRepo seasonRepo;
  private final LeagueRepo leagueRepo;
  private final CrawlerService crawlerService;

  @Transactional
  public List<Season> createSeasons() {
    List<League> leagues = leagueRepo.findAll();
    if (leagues.isEmpty()) {
      log.warn("No leagues found");
      return Collections.emptyList();
    }

    List<Season> seasons = new ArrayList<>();
    for (League league : leagues) {
      List<LocalDate> matchDays = crawlerService.pullCurrentSeasonScheduleMatchDays(league.getCode());
      Season record = new Season();
      record.setMatchDays(matchDays);
      record = seasonRepo.save(record);
      seasons.add(record);
    }

    return seasons;
  }

  public Season save(Season season) {
    return seasonRepo.save(season);
  }

  public Season getSeason(int year) {
    return seasonRepo.getByYear(String.valueOf(year));
  }

  public Season getCurrentSeason() {
    int year = LocalDate.now().getYear();
    return seasonRepo.getByYear(String.valueOf(year));
  }
}
