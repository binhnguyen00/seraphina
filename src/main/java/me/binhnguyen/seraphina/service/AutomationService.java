package me.binhnguyen.seraphina.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.binhnguyen.seraphina.entity.Matchup;
import me.binhnguyen.seraphina.entity.ZaloChat;
import me.binhnguyen.seraphina.repository.MatchupRepo;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AutomationService {
  private final MatchupRepo matchupRepo;
  private final ZaloChatService zaloService;
  private final SeasonService seasonService;
  private final CrawlerService crawlerService;
  private final PremierLeagueService premierLeagueService;

  /* Cron Example
   * runs daily at 07:00
   * 0 → seconds
   * 0 → minutes
   * 7 → hour (07:00)
   * '*' → every day of the month
   * '*' → every month
   * '?' → no specific day-of-week
   */

  /**
   * Auto update match days. Match days are pre-saved on server init.
   * But sometime realtime match day could be changed. This make sure match day are up-to-date.
   * Runs weekly on MON, at 07:00
   */
  @Transactional
  @Scheduled(cron = "0 0 7 * * MON")
  public void refreshMatchDays() {
    premierLeagueService.createOrUpdateAllMatchDays();
  }

  /**
   * Auto create/update current week matchups by pull data from ESPN.
   * Apply to all available leagues.
   * Runs daily at 07:00
   */
  @Transactional
  @Scheduled(cron = "0 0 7 * * ?")
  public void createOrUpdateCurrentWeekMatchups() {
    LocalDate today = LocalDate.now();
    LocalDate THIS_MONDAY = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    LocalDate THIS_SUNDAY = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
    premierLeagueService.createOrUpdateMatchups(THIS_MONDAY, THIS_SUNDAY);
  }

  /**
   * runs at 07:00 on every Friday, Saturday, and Sunday. every month
   */
  @Deprecated
  @Transactional
  @Scheduled(cron = "0 0 9 ? * FRI,SAT,SUN")
  public void notifyZalo() {
    List<Matchup> thisWeekMatches = premierLeagueService.getCurrentWeekMatches();
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < thisWeekMatches.size(); i++) {
      Matchup match = thisWeekMatches.get(i);
      builder.append(String.format("""
      %d ------------------------------------
      Đội nhà\t%s
      Đội khách\t%s
      Giờ đá\t%s
      Địa điểm\t%s
      
      """,
        i + 1,
        match.getHomeTeam().getName(),
        match.getAwayTeam().getName(),
        match.getFormatMatchDay(),
        match.getHomeStadium()
      ));
    }
    List<ZaloChat> subscribers = zaloService.getAllSubscribers();
    zaloService.sendMessageTo(subscribers, builder.toString());
  }
}
