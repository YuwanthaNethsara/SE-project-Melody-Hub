package com.example.project1.controller;

import com.example.project1.dto.DashboardStatsResponse;
import com.example.project1.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** ADMIN only (every /api/admin/** path is restricted in SecurityConfig). */
@RestController
@RequestMapping("/api/admin/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public ResponseEntity<DashboardStatsResponse> stats() {
        return ResponseEntity.ok(dashboardService.getAdminStats());
    }
}
