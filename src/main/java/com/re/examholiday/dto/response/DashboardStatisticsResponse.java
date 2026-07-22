package com.re.examholiday.dto.response;

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
public class DashboardStatisticsResponse {
    private List<RevenueDataPoint> revenueByDay;
    private List<RevenueDataPoint> revenueByMonth;
    private List<RevenueDataPoint> revenueByYear;
    private List<OrderStatusDistributionDto> orderStatusDistribution;
    private List<CategorySalesDto> categorySales;
    private List<RecentActivityDto> recentActivities;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderStatusDistributionDto {
        private String status;
        private String label;
        private long count;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategorySalesDto {
        private String categoryName;
        private BigDecimal revenue;
        private long quantitySold;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecentActivityDto {
        private String title;
        private String description;
        private String timeStr;
        private String type; // SUCCESS, INFO, WARNING, DANGER
        private String icon; // Bootstrap icon
    }
}

