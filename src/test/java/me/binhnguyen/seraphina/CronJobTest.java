package me.binhnguyen.seraphina;

import org.junit.jupiter.api.Test;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

public class CronJobTest {

  @Test
  void quartzTest() {
    try {
      Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
      scheduler.start();
      scheduler.shutdown();
    } catch (SchedulerException se) {
      se.printStackTrace();
    }
  }
}
