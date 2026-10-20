package com.example.demoapp.controller;

import com.example.demoapp.service.DashboardService;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

  private final DashboardService dashboardService;

  public DashboardController(DashboardService dashboardService) {
    this.dashboardService = dashboardService;
  }

  @GetMapping("/summary")
  public ResponseEntity<Map<String, Object>> getSummary() {
    return ResponseEntity.ok(dashboardService.getSummary());
  }
}
