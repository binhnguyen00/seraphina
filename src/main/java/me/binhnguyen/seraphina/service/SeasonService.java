package me.binhnguyen.seraphina.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.binhnguyen.seraphina.entity.Season;
import me.binhnguyen.seraphina.repository.LeagueRepo;
import me.binhnguyen.seraphina.repository.SeasonRepo;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeasonService {
  private final SeasonRepo seasonRepo;
  private final LeagueRepo leagueRepo;
  private final CrawlerService crawlerService;

  @Transactional
  public Season createSeason() {
    int year = LocalDate.now().getYear();
    Season exist = seasonRepo.getByYear(String.valueOf(LocalDate.now().getYear()));
    if (!Objects.isNull(exist)) {
      log.warn("Season {} already exists", year);
      return exist;
    }
    return this.save(new Season());
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
