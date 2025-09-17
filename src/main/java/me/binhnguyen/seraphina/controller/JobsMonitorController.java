package me.binhnguyen.seraphina.controller;

import lombok.RequiredArgsConstructor;
import me.binhnguyen.seraphina.common.Response;
import me.binhnguyen.seraphina.service.JobsMonitorService;
import org.quartz.SchedulerException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** URL Endpoint <code> /api/v1/monitor/jobs </code> */
@RestController
@RequestMapping(BaseController.REST_URL + "/monitor/jobs")
@RequiredArgsConstructor
public class JobsMonitorController extends BaseController {
  private final JobsMonitorService monitorService;

  @GetMapping
  public ResponseEntity<Response> getAllJobs() throws SchedulerException {
    return ResponseEntity.ok(Response.SUCCESS(
      "Get All Jobs successfully",
      monitorService.getAllJobs()
    ));
  }
}
