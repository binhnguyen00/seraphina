package me.binhnguyen.seraphina.controller;

import lombok.RequiredArgsConstructor;
import me.binhnguyen.seraphina.common.DataRecord;
import me.binhnguyen.seraphina.service.JobsMonitorService;
import org.quartz.SchedulerException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/monitor/jobs")
@RequiredArgsConstructor
public class JobsMonitorController extends BaseController {
  private final JobsMonitorService monitorService;

  @GetMapping
  public ResponseEntity<DataRecord> getAllJobs() throws SchedulerException {
    return ResponseEntity.ok(
      DataRecord.spawn()
        .with("success", true)
        .with("message", "Get All Jobs successfully")
        .with("data", monitorService.getAllJobs())
    );
  }
}
