package me.binhnguyen.seraphina.jobs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.binhnguyen.seraphina.service.ChampionLeagueService;
import me.binhnguyen.seraphina.service.LaligaService;
import me.binhnguyen.seraphina.service.PremierLeagueService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobCreateOrUpdateCurrentWeekMatchups implements Job {
  private final PremierLeagueService premierLeagueService;
  private final LaligaService laligaService;
  private final ChampionLeagueService championLeagueService;

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    try {
      Date fireTime = context.getFireTime();
      LocalDate today = LocalDate.ofInstant(fireTime.toInstant(), ZoneId.systemDefault());
      LocalDate THIS_MONDAY = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
      LocalDate THIS_SUNDAY = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
      premierLeagueService.createOrUpdateMatchups(THIS_MONDAY, THIS_SUNDAY);
      laligaService.createOrUpdateMatchups(THIS_MONDAY, THIS_SUNDAY);
      championLeagueService.createOrUpdateMatchups(THIS_MONDAY, THIS_SUNDAY);
    } catch (Exception e) {
      throw new JobExecutionException(e);
    }
  }
}
