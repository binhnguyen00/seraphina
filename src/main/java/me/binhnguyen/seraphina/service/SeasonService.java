package me.binhnguyen.seraphina.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import me.binhnguyen.seraphina.entity.Season;
import me.binhnguyen.seraphina.repository.SeasonRepo;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SeasonService {
  private final CrawlerService crawlerService;
  private final SeasonRepo seasonRepo;

  @Transactional
  public Season createSeason() {
    ArrayList<LocalDate> matchDays = crawlerService.getCurrentSeasonScheduleMatchDays();
    Season record = new Season();
    record.setMatchDays(matchDays);
    record = seasonRepo.save(record);
    return record;
  }

  public Season save(Season season) {
    return seasonRepo.save(season);
  }

  /** Get current season or create new season */
  public Season getOrCreate() {
    Season season = getCurrentSeason();
    if (Objects.isNull(season))
      season = this.createSeason();
    return season;
  }

  public Season getSeason(int year) {
    return seasonRepo.getByYear(year);
  }

  public Season getCurrentSeason() {
    int year = LocalDate.now().getYear();
    return seasonRepo.getByYear(year);
  }
}
