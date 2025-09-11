package me.binhnguyen.seraphina.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.binhnguyen.seraphina.entity.MatchDay;
import me.binhnguyen.seraphina.entity.Matchup;
import me.binhnguyen.seraphina.entity.Season;
import me.binhnguyen.seraphina.entity.ZaloChat;
import me.binhnguyen.seraphina.repository.MatchupRepo;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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

  /**
   * runs at 00:00:00 on the first day of every month
   */
  @Transactional
  @Scheduled(cron = "0 0 0 1 * ?")
  public void updateCurrentSeason() {
    List<LocalDate> matchDays = crawlerService.pullCurrentSeasonScheduleMatchDays(PremierLeagueService.LEAGUE_ID);
    Season record = seasonService.getCurrentSeason();
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
  public void createOrUpdateMatches() {
    List<MatchDay> matchDays = premierLeagueService.getThisWeekMatchDays();
    if (matchDays.isEmpty()) return;
    premierLeagueService.createOrUpdateMatchups(
      matchDays.getFirst().getDate(),
      matchDays.getLast().getDate()
    );
  }

  /**
   * runs at 07:00 on every Friday, Saturday, and Sunday. every month
   */
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
