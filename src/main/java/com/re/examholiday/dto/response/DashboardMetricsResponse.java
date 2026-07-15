package com.re.examholiday.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardMetricsResponse {
    private BigDecimal totalRevenue;
    private long totalOrders;
    private long totalCustomers;
    private long totalEmployees;
    private long totalMenuItems;
    private long totalTables;
    private long todayOrderCount;
    private BigDecimal todayRevenue;
}
