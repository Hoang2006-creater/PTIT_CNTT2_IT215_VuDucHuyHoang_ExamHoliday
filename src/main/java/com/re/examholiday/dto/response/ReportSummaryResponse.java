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
public class ReportSummaryResponse {
    private BigDecimal revenue;
    private List<BestSellingItemResponse> bestSellingItems;
    private List<MostUsedTableResponse> mostUsedTables;
    private List<LoyalCustomerResponse> loyalCustomers;
    private List<TopEmployeeResponse> topEmployees;
}
