package me.binhnguyen.seraphina.jobs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.binhnguyen.seraphina.entity.Matchup;
import me.binhnguyen.seraphina.entity.Subscriber;
import me.binhnguyen.seraphina.service.PremierLeagueService;
import me.binhnguyen.seraphina.service.SubscriberService;
import me.binhnguyen.seraphina.utils.MessageTemplate;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobNotifyZalo implements Job {
  private final SubscriberService zaloService;
  private final PremierLeagueService premierLeagueService;

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    try {
      List<Matchup> thisWeekMatches = premierLeagueService.getCurrentWeekMatches();
      String message = MessageTemplate.ZALO(thisWeekMatches);
      List<Subscriber> subscribers = zaloService.getAllSubscribers();
      zaloService.sendMessageTo(subscribers, message);
    } catch (Exception e) {
      throw new JobExecutionException(e);
    }
  }
}
