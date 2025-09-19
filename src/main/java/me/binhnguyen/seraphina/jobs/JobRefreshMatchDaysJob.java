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

@Slf4j
@Component
@RequiredArgsConstructor
public class JobRefreshMatchDaysJob implements Job {
  private final PremierLeagueService premierLeagueService;
  private final LaligaService laligaService;
  private final ChampionLeagueService championLeagueService;

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    try {
      premierLeagueService.createOrUpdateAllMatchDays();
      laligaService.createOrUpdateAllMatchDays();
      championLeagueService.createOrUpdateAllMatchDays();
    } catch (Exception e) {
      throw new JobExecutionException(e);
    }
  }
}
