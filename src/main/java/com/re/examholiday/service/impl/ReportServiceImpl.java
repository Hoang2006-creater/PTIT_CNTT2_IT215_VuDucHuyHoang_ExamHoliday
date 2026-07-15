package com.re.examholiday.service.impl;

import com.re.examholiday.dto.response.*;
import com.re.examholiday.model.Customer;
import com.re.examholiday.model.Employee;
import com.re.examholiday.model.MenuItem;
import com.re.examholiday.model.RestaurantTable;
import com.re.examholiday.model.enumeration.OrderStatus;
import com.re.examholiday.model.enumeration.PaymentStatus;
import com.re.examholiday.repository.OrderDetailRepository;
import com.re.examholiday.repository.OrderRepository;
import com.re.examholiday.repository.PaymentRepository;
import com.re.examholiday.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl implements ReportService {

    private final PaymentRepository paymentRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final OrderRepository orderRepository;

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ApiResponse<ReportSummaryResponse> getReportSummary(LocalDate startDate, LocalDate endDate) {
        // Fallback to current month if null
        if (startDate == null) {
            startDate = LocalDate.now().withDayOfMonth(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        LocalDateTime startTime = startDate.atStartOfDay();
        LocalDateTime endTime = endDate.atTime(LocalTime.MAX);

        log.info("Generating report summary from {} to {}", startTime, endTime);

        // 1. Calculate Revenue
        BigDecimal revenue = paymentRepository.sumRevenueByTimeRange(PaymentStatus.SUCCESS, startTime, endTime);
        if (revenue == null) {
            revenue = BigDecimal.ZERO;
        }

        // 2. Calculate Best Selling Menu Items (Top 10)
        List<Object[]> rawItems = orderDetailRepository.findBestSellingMenuItems(startTime, endTime, PageRequest.of(0, 10));
        List<BestSellingItemResponse> bestSellingItems = rawItems.stream().map(obj -> {
            MenuItem menuItem = (MenuItem) obj[0];
            Long totalSold = (Long) obj[1];
            if (menuItem == null) return null;
            return BestSellingItemResponse.builder()
                    .menuItemId(menuItem.getId())
                    .name(menuItem.getName())
                    .price(menuItem.getPrice())
                    .totalSold(totalSold != null ? totalSold : 0L)
                    .build();
        })
        .filter(java.util.Objects::nonNull)
        .collect(Collectors.toList());

        // 3. Calculate Most Used Tables (Top 10)
        List<Object[]> rawTables = orderRepository.findMostUsedTables(startTime, endTime, PageRequest.of(0, 10));
        List<MostUsedTableResponse> mostUsedTables = rawTables.stream().map(obj -> {
            RestaurantTable table = (RestaurantTable) obj[0];
            Long orderCount = (Long) obj[1];
            if (table == null) return null;
            return MostUsedTableResponse.builder()
                    .tableId(table.getId())
                    .tableNumber(table.getTableNumber())
                    .capacity(table.getCapacity())
                    .orderCount(orderCount != null ? orderCount : 0L)
                    .build();
        })
        .filter(java.util.Objects::nonNull)
        .collect(Collectors.toList());

        // 4. Calculate Loyal Customers (Top 10)
        List<Object[]> rawCustomers = orderRepository.findLoyalCustomers(OrderStatus.COMPLETED, startTime, endTime, PageRequest.of(0, 10));
        List<LoyalCustomerResponse> loyalCustomers = rawCustomers.stream().map(obj -> {
            Customer customer = (Customer) obj[0];
            BigDecimal totalSpent = (BigDecimal) obj[1];
            Long orderCount = (Long) obj[2];
            if (customer == null) return null;
            return LoyalCustomerResponse.builder()
                    .customerId(customer.getId())
                    .fullName(customer.getFullName())
                    .phone(customer.getPhone())
                    .totalSpent(totalSpent != null ? totalSpent : BigDecimal.ZERO)
                    .orderCount(orderCount != null ? orderCount : 0L)
                    .build();
        })
        .filter(java.util.Objects::nonNull)
        .collect(Collectors.toList());

        // 5. Calculate Top Employees (Top 10)
        List<Object[]> rawEmployees = orderRepository.findTopEmployees(startTime, endTime, PageRequest.of(0, 10));
        List<TopEmployeeResponse> topEmployees = rawEmployees.stream().map(obj -> {
            Employee employee = (Employee) obj[0];
            Long orderCount = (Long) obj[1];
            if (employee == null) return null;
            return TopEmployeeResponse.builder()
                    .employeeId(employee.getId())
                    .fullName(employee.getFullName())
                    .position(employee.getPosition())
                    .orderCount(orderCount != null ? orderCount : 0L)
                    .build();
        })
        .filter(java.util.Objects::nonNull)
        .collect(Collectors.toList());

        ReportSummaryResponse summary = ReportSummaryResponse.builder()
                .revenue(revenue)
                .bestSellingItems(bestSellingItems)
                .mostUsedTables(mostUsedTables)
                .loyalCustomers(loyalCustomers)
                .topEmployees(topEmployees)
                .build();

        return ApiResponse.success("Lấy báo cáo thống kê thành công", summary);
    }
}
