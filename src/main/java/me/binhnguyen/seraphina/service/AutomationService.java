package me.binhnguyen.seraphina.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.binhnguyen.seraphina.entity.Matchup;
import me.binhnguyen.seraphina.entity.ZaloChat;
import me.binhnguyen.seraphina.utils.MessageTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class AutomationService {
  private final ZaloChatService zaloService;
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
   * Notify Zalo subscribers about upcoming matches
   * Run daily at 00:00
   */
  @Scheduled(cron = "0 0 0 * * *")
  public void notifyZalo() {
    List<Matchup> thisWeekMatches = premierLeagueService.getCurrentWeekMatches();
    String message = MessageTemplate.ZALO(thisWeekMatches);
    List<ZaloChat> subscribers = zaloService.getAllSubscribers();
    zaloService.sendMessageTo(subscribers, message);
  }
}
