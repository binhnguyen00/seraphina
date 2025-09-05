package me.binhnguyen.seraphina.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import me.binhnguyen.seraphina.entity.Season;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class AutomationService {
  private final SeasonService seasonService;
  private final CrawlerService crawlerService;

  /** runs at 00:00:00 on the first day of every month */
  @Transactional
  @Scheduled(cron = "0 0 0 1 * ?")
  public void updateCurrentSeason() {
    ArrayList<LocalDate> matchDays = crawlerService.getCurrentSeasonScheduleMatchDays();
    Season record = seasonService.getCurrentSeason();
    record.setMatchDays(matchDays);
    seasonService.save(record);
  }
}
