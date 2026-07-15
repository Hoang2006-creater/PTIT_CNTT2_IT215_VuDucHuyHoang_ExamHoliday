package com.re.examholiday.controller;

import com.re.examholiday.dto.response.ApiResponse;
import com.re.examholiday.dto.response.DashboardMetricsResponse;
import com.re.examholiday.dto.response.DashboardStatisticsResponse;
import com.re.examholiday.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class AnalyticsRestController {

    private final AnalyticsService analyticsService;

    /**
     * GET /api/analytics/metrics - Lấy tổng hợp các chỉ số hoạt động
     */
    @GetMapping("/metrics")
    public ResponseEntity<ApiResponse<DashboardMetricsResponse>> getDashboardMetrics() {
        ApiResponse<DashboardMetricsResponse> response = analyticsService.getDashboardMetrics();
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/analytics/statistics - Lấy thống kê doanh thu theo ngày, tháng, năm
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<DashboardStatisticsResponse>> getDashboardStatistics() {
        ApiResponse<DashboardStatisticsResponse> response = analyticsService.getDashboardStatistics();
        return ResponseEntity.ok(response);
    }
}
