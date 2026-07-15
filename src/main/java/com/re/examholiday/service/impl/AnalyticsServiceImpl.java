package com.re.examholiday.service.impl;

import com.re.examholiday.dto.response.ApiResponse;
import com.re.examholiday.dto.response.DashboardMetricsResponse;
import com.re.examholiday.dto.response.DashboardStatisticsResponse;
import com.re.examholiday.dto.response.RevenueDataPoint;
import com.re.examholiday.model.Payment;
import com.re.examholiday.model.enumeration.PaymentStatus;
import com.re.examholiday.repository.*;
import com.re.examholiday.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsServiceImpl implements AnalyticsService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
    private final MenuItemRepository menuItemRepository;
    private final RestaurantTableRepository restaurantTableRepository;

    @Override
    public ApiResponse<DashboardMetricsResponse> getDashboardMetrics() {
        LocalDate today = LocalDate.now();

        // 1. Fetch counts
        long totalOrders = orderRepository.count();
        long totalCustomers = customerRepository.count();
        long totalEmployees = employeeRepository.count();
        long totalMenuItems = menuItemRepository.count();
        long totalTables = restaurantTableRepository.count();

        // 2. Fetch payments for revenue calculations
        List<Payment> allPayments = paymentRepository.findAll();

        BigDecimal totalRevenue = allPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS && p.getAmount() != null)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal todayRevenue = allPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS 
                        && p.getPaymentTime() != null 
                        && p.getPaymentTime().toLocalDate().isEqual(today)
                        && p.getAmount() != null)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. Fetch today's order count
        long todayOrderCount = orderRepository.findAll().stream()
                .filter(o -> o.getOrderTime() != null && o.getOrderTime().toLocalDate().isEqual(today))
                .count();

        DashboardMetricsResponse metrics = DashboardMetricsResponse.builder()
                .totalRevenue(totalRevenue)
                .totalOrders(totalOrders)
                .totalCustomers(totalCustomers)
                .totalEmployees(totalEmployees)
                .totalMenuItems(totalMenuItems)
                .totalTables(totalTables)
                .todayOrderCount(todayOrderCount)
                .todayRevenue(todayRevenue)
                .build();

        return ApiResponse.success("Lấy các chỉ số dashboard thành công", metrics);
    }

    @Override
    public ApiResponse<DashboardStatisticsResponse> getDashboardStatistics() {
        int currentYear = LocalDate.now().getYear();

        // Fetch successful payments
        List<Payment> successfulPayments = paymentRepository.findAll().stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS && p.getPaymentTime() != null && p.getAmount() != null)
                .collect(Collectors.toList());

        // 1. Revenue by Day (Last 30 days)
        Map<LocalDate, BigDecimal> dailyMap = successfulPayments.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getPaymentTime().toLocalDate(),
                        Collectors.reducing(BigDecimal.ZERO, Payment::getAmount, BigDecimal::add)
                ));

        List<RevenueDataPoint> revenueByDay = IntStream.range(0, 30)
                .mapToObj(i -> LocalDate.now().minusDays(i))
                .map(date -> RevenueDataPoint.builder()
                        .label(date.toString())
                        .revenue(dailyMap.getOrDefault(date, BigDecimal.ZERO))
                        .build())
                .collect(Collectors.toList());
        Collections.reverse(revenueByDay);

        // 2. Revenue by Month (Current year)
        Map<Integer, BigDecimal> monthlyMap = successfulPayments.stream()
                .filter(p -> p.getPaymentTime().getYear() == currentYear)
                .collect(Collectors.groupingBy(
                        p -> p.getPaymentTime().getMonthValue(),
                        Collectors.reducing(BigDecimal.ZERO, Payment::getAmount, BigDecimal::add)
                ));

        List<RevenueDataPoint> revenueByMonth = IntStream.rangeClosed(1, 12)
                .mapToObj(month -> RevenueDataPoint.builder()
                        .label(String.format("Tháng %02d/%d", month, currentYear))
                        .revenue(monthlyMap.getOrDefault(month, BigDecimal.ZERO))
                        .build())
                .collect(Collectors.toList());

        // 3. Revenue by Year (All years)
        Map<Integer, BigDecimal> yearlyMap = successfulPayments.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getPaymentTime().getYear(),
                        Collectors.reducing(BigDecimal.ZERO, Payment::getAmount, BigDecimal::add)
                ));

        List<RevenueDataPoint> revenueByYear = yearlyMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> RevenueDataPoint.builder()
                        .label("Năm " + entry.getKey())
                        .revenue(entry.getValue())
                        .build())
                .collect(Collectors.toList());

        if (revenueByYear.isEmpty()) {
            revenueByYear.add(RevenueDataPoint.builder()
                    .label("Năm " + currentYear)
                    .revenue(BigDecimal.ZERO)
                    .build());
        }

        DashboardStatisticsResponse statistics = DashboardStatisticsResponse.builder()
                .revenueByDay(revenueByDay)
                .revenueByMonth(revenueByMonth)
                .revenueByYear(revenueByYear)
                .build();

        return ApiResponse.success("Lấy số liệu thống kê doanh thu thành công", statistics);
    }
}
