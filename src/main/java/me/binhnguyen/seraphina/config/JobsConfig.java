package me.binhnguyen.seraphina.config;

import me.binhnguyen.seraphina.jobs.JobCreateOrUpdateCurrentWeekMatchups;
import me.binhnguyen.seraphina.jobs.JobNotifyZalo;
import me.binhnguyen.seraphina.jobs.JobRefreshMatchDaysJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JobsConfig {

  /**
   * Auto update match days. Match days are pre-saved on server init.
   * But sometime realtime match day could be changed. This make sure match day are up-to-date.
   * Runs weekly on MON, at 07:00
   */
  @Bean
  public JobDetail refreshMatchDaysJob() {
    return JobBuilder.newJob(JobRefreshMatchDaysJob.class)
      .withIdentity("refreshMatchDays")
      .storeDurably()
      .build();
  }

  @Bean
  public Trigger matchupUpdateTrigger() {
    return TriggerBuilder.newTrigger()
      .forJob(refreshMatchDaysJob())
      .withIdentity("refreshMatchDaysTrigger")
      .withSchedule(CronScheduleBuilder.cronSchedule("0 0 7 ? * MON"))
      .build();
  }

  /**
   * Auto create/update current week matchups by pull data from ESPN.
   * Apply to all available leagues.
   * Runs daily at 07:00
   */
  @Bean
  public JobDetail createOrUpdateCurrentWeekMatchupsJob() {
    return JobBuilder.newJob(JobCreateOrUpdateCurrentWeekMatchups.class)
      .withIdentity("createOrUpdateCurrentWeekMatchups")
      .storeDurably()
      .build();
  }

  @Bean
  public Trigger createOrUpdateCurrentWeekMatchupsTrigger() {
    return TriggerBuilder.newTrigger()
      .forJob(createOrUpdateCurrentWeekMatchupsJob())
      .withIdentity("createOrUpdateCurrentWeekMatchupsTrigger")
      .withSchedule(CronScheduleBuilder.cronSchedule("0 0 7 * * ?"))
      .build();
  }

  /**
   * Notify Zalo subscribers about upcoming matches
   * Run daily at 00:00
   */
  @Bean
  public JobDetail notifyZaloJob() {
    return JobBuilder.newJob(JobNotifyZalo.class)
      .withIdentity("notifyZalo")
      .storeDurably()
      .build();
  }

  @Bean
  public Trigger notifyZaloTrigger() {
    return TriggerBuilder.newTrigger()
      .forJob(notifyZaloJob())
      .withIdentity("notifyZaloTrigger")
      .withSchedule(CronScheduleBuilder.cronSchedule("0 0 0 * * ?"))
      .build();
  }
}
