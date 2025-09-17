package me.binhnguyen.seraphina.controller;

import me.binhnguyen.seraphina.common.DataRecord;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(BaseController.REST_URL)
public class BaseController {
  /** URL Endpoint <code> /api/v1 </code> */
  public static final String REST_URL = "/api/v1";

  @GetMapping("/health")
  public ResponseEntity<DataRecord> healthCheck() {
    return ResponseEntity.ok(
      DataRecord.spawn()
        .with("success", true)
        .with("message", "Healthy")
    );
  }
}