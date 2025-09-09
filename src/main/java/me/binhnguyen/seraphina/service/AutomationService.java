package me.binhnguyen.seraphina.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import me.binhnguyen.seraphina.entity.Matchup;
import me.binhnguyen.seraphina.entity.Season;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AutomationService {
  private final ZaloChatService zaloService;
  private final SeasonService seasonService;
  private final CrawlerService crawlerService;
  private final PremierLeagueService premierLeagueService;

  /**
   * runs at 00:00:00 on the first day of every month
   */
  @Transactional
  @Scheduled(cron = "0 0 0 1 * ?")
  public void updateCurrentSeason() {
    List<LocalDate> matchDays = crawlerService.getCurrentSeasonScheduleMatchDays();
    Season record = seasonService.getCurrentSeason();
    record.setMatchDays(matchDays);
    seasonService.save(record);
  }

  /**
   * runs daily at 07:00
   * 0 → seconds
   * 0 → minutes
   * 7 → hour (07:00)
   * '*' → every day of the month
   * '*' → every month
   * '?' → no specific day-of-week
   */
  @Transactional
  @Scheduled(cron = "0 0 7 * * ?")
  public void updateMatches() {
    Season season = seasonService.getCurrentSeason();
    premierLeagueService.createOrUpdateMatches(season);
  }

  /**
   * runs at 07:00 on every Friday, Saturday, and Sunday. every month
   */
  @Transactional
  @Scheduled(cron = "0 0 9 ? * FRI,SAT,SUN")
  public void notifyZalo() {
    Season season = seasonService.getCurrentSeason();
    List<Matchup> thisWeekMatches = premierLeagueService.getMatches(season);
    StringBuilder stringBuild = new StringBuilder();
    for (Matchup match : thisWeekMatches) {
      stringBuild.append("⚽️⚽️⚽️⚽️⚽️⚽️⚽️⚽️⚽️⚽️⚽️⚽️⚽️⚽️⚽️");
      stringBuild.append(String.format("Đội nhà %s", match.getHome()));
      stringBuild.append(String.format("Đội khách %s", match.getAway()));
      stringBuild.append(String.format("Giờ đá %s", match.getMatchDay()));
      stringBuild.append(String.format("Địa điểm %s", match.getHomeStadium()));
      stringBuild.append("\n");
    }
    zaloService.sendMessage(stringBuild.toString());
  }
}
