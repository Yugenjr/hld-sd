package com.smartev.platform.dashboard;

import com.smartev.platform.common.ApiResponse;
import com.smartev.platform.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@Tag(name = "Dashboard", description = "Endpoints for aggregated overview statistics and telemetry analytics")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/metrics")
    @Operation(summary = "Get aggregated driver and network metrics from persisted data")
    public ResponseEntity<ApiResponse<DashboardMetricsDTO>> getDashboardMetrics(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        DashboardMetricsDTO metrics = dashboardService.getMetrics(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok("Dashboard metrics calculated successfully", metrics));
    }
}
