package me.binhnguyen.seraphina.jobs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.binhnguyen.seraphina.common.DataRecord;
import me.binhnguyen.seraphina.entity.Subscriber;
import me.binhnguyen.seraphina.service.ChampionLeagueService;
import me.binhnguyen.seraphina.service.LaligaService;
import me.binhnguyen.seraphina.service.PremierLeagueService;
import me.binhnguyen.seraphina.service.SubscriberService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobNotifyZalo implements Job {
  private final SubscriberService zaloService;
  private final PremierLeagueService premierLeagueService;
  private final LaligaService laligaService;
  private final ChampionLeagueService championLeagueService;
  private final SubscriberService subscriberService;

  @Override
  public void execute(JobExecutionContext context) {
    final LocalDate tomorrow = LocalDate.now().plusDays(1);
    List<Subscriber> subscribers = zaloService.getAllSubscribers();
    for (Subscriber subscriber : subscribers) {
      List<DataRecord> leagues = subscriberService.getLeagues(subscriber, tomorrow, tomorrow);
      try {
        zaloService.sendMessageTo(subscriber, leagues);
      } catch (Exception e) {
        log.error("Failed to send message to chat {}, {}", subscriber.getLookupId(), subscriber.getName());
      }
    }
  }
}
