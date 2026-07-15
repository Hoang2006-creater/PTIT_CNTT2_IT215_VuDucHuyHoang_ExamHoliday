package com.re.examholiday.service;

import com.re.examholiday.dto.response.ApiResponse;
import com.re.examholiday.dto.response.ReportSummaryResponse;

import java.time.LocalDate;

public interface ReportService {

    ApiResponse<ReportSummaryResponse> getReportSummary(LocalDate startDate, LocalDate endDate);
}
