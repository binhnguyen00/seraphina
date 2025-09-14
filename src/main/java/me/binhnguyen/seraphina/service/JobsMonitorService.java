package me.binhnguyen.seraphina.service;

import lombok.RequiredArgsConstructor;
import me.binhnguyen.seraphina.common.DataRecord;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JobsMonitorService {
  private final Scheduler scheduler;

  public List<DataRecord> getAllJobs() throws SchedulerException {
    List<DataRecord> jobs = new ArrayList<>();
    for (String group : scheduler.getJobGroupNames()) {
      for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(group))) {
        List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
        for (Trigger trigger : triggers) {
          DataRecord info = new DataRecord(
            Map.of(
              "jobName", jobKey.getName(),
              "group", jobKey.getGroup(),
              "triggerName", trigger.getKey().getName(),
              "triggerGroup", trigger.getKey().getGroup(),
              "nextFireTime", trigger.getNextFireTime(),
              "previousFireTime", trigger.getPreviousFireTime(),
              "state", scheduler.getTriggerState(trigger.getKey()).name()
            )
          );
          jobs.add(info);
        }
      }
    }
    return jobs;
  }

  public void printJobs() throws SchedulerException {
    for (String group : scheduler.getJobGroupNames()) {
      for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(group))) {
        List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
        for (Trigger trigger : triggers) {
          System.out.printf(
            "Job: %s | Next: %s | Previous: %s | State: %s%n",
            jobKey.getName(),
            trigger.getNextFireTime(),
            trigger.getPreviousFireTime(),
            scheduler.getTriggerState(trigger.getKey())
          );
        }
      }
    }
  }
}
