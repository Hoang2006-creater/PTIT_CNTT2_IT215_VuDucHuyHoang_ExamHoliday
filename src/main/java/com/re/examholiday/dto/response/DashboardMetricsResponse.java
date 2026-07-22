package com.re.examholiday.dto.response;

import com.re.examholiday.model.enumeration.TableStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

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
    private long servingTablesCount;
    private long emptyTablesCount;
    private long reservedTablesCount;
    private long kitchenPendingItemsCount;
    private long todayOrderCount;
    private BigDecimal todayRevenue;
    private List<TableMetricDto> tables;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TableMetricDto {
        private Integer id;
        private String tableNumber;
        private Integer capacity;
        private TableStatus status;
        private String location;
    }
}

