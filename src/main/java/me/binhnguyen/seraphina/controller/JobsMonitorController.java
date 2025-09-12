package me.binhnguyen.seraphina.controller;

import lombok.RequiredArgsConstructor;
import me.binhnguyen.seraphina.common.DataRecord;
import me.binhnguyen.seraphina.service.JobsMonitorService;
import org.quartz.SchedulerException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/monitor/jobs")
@RequiredArgsConstructor
public class JobsMonitorController extends BaseController {
  private final JobsMonitorService monitorService;

  @GetMapping
  public List<DataRecord> getAllJobs() throws SchedulerException {
    return monitorService.getAllJobs();
  }
}
