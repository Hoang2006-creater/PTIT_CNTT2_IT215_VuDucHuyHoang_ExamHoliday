package com.re.examholiday.service;

import com.re.examholiday.dto.response.ApiResponse;
import com.re.examholiday.dto.response.DashboardMetricsResponse;
import com.re.examholiday.dto.response.DashboardStatisticsResponse;

public interface AnalyticsService {

    ApiResponse<DashboardMetricsResponse> getDashboardMetrics();

    ApiResponse<DashboardStatisticsResponse> getDashboardStatistics();
}
