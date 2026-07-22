package com.re.examholiday.service.impl;

import com.re.examholiday.dto.response.ApiResponse;
import com.re.examholiday.dto.response.DashboardMetricsResponse;
import com.re.examholiday.dto.response.DashboardStatisticsResponse;
import com.re.examholiday.dto.response.RevenueDataPoint;
import com.re.examholiday.model.Order;
import com.re.examholiday.model.OrderDetail;
import com.re.examholiday.model.Payment;
import com.re.examholiday.model.RestaurantTable;
import com.re.examholiday.model.enumeration.OrderItemStatus;
import com.re.examholiday.model.enumeration.OrderStatus;
import com.re.examholiday.model.enumeration.PaymentStatus;
import com.re.examholiday.model.enumeration.TableStatus;
import com.re.examholiday.repository.*;
import com.re.examholiday.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsServiceImpl implements AnalyticsService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
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
        
        // Table status metrics
        List<RestaurantTable> allTables = restaurantTableRepository.findAll();
        long totalTables = allTables.size();

        long servingTablesCount = allTables.stream()
                .filter(t -> t.getStatus() == TableStatus.OCCUPIED)
                .count();

        long emptyTablesCount = allTables.stream()
                .filter(t -> t.getStatus() == TableStatus.EMPTY)
                .count();

        long reservedTablesCount = allTables.stream()
                .filter(t -> t.getStatus() == TableStatus.RESERVED)
                .count();

        // Kitchen pending items count (ORDERED or COOKING)
        List<OrderDetail> allOrderDetails = orderDetailRepository.findAll();
        long kitchenPendingItemsCount = allOrderDetails.stream()
                .filter(od -> od.getStatus() == OrderItemStatus.ORDERED || od.getStatus() == OrderItemStatus.COOKING)
                .mapToInt(OrderDetail::getQuantity)
                .sum();

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

        // 4. Map tables list for visual map grid
        List<DashboardMetricsResponse.TableMetricDto> tableDtos = allTables.stream()
                .sorted(Comparator.comparing(RestaurantTable::getId))
                .map(t -> DashboardMetricsResponse.TableMetricDto.builder()
                        .id(t.getId())
                        .tableNumber(t.getTableNumber())
                        .capacity(t.getCapacity())
                        .status(t.getStatus())
                        .location(t.getLocation())
                        .build())
                .collect(Collectors.toList());

        DashboardMetricsResponse metrics = DashboardMetricsResponse.builder()
                .totalRevenue(totalRevenue)
                .totalOrders(totalOrders)
                .totalCustomers(totalCustomers)
                .totalEmployees(totalEmployees)
                .totalMenuItems(totalMenuItems)
                .totalTables(totalTables)
                .servingTablesCount(servingTablesCount)
                .emptyTablesCount(emptyTablesCount)
                .reservedTablesCount(reservedTablesCount)
                .kitchenPendingItemsCount(kitchenPendingItemsCount)
                .todayOrderCount(todayOrderCount)
                .todayRevenue(todayRevenue)
                .tables(tableDtos)
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

        // 4. Order Status Distribution Chart
        List<Order> allOrders = orderRepository.findAll();
        Map<OrderStatus, Long> statusCountsMap = allOrders.stream()
                .collect(Collectors.groupingBy(Order::getStatus, Collectors.counting()));

        List<DashboardStatisticsResponse.OrderStatusDistributionDto> orderStatusDistribution = Arrays.asList(
                DashboardStatisticsResponse.OrderStatusDistributionDto.builder()
                        .status("OPEN")
                        .label("Đang phục vụ / Đang mở")
                        .count(statusCountsMap.getOrDefault(OrderStatus.OPEN, 0L))
                        .build(),
                DashboardStatisticsResponse.OrderStatusDistributionDto.builder()
                        .status("COMPLETED")
                        .label("Đã hoàn thành")
                        .count(statusCountsMap.getOrDefault(OrderStatus.COMPLETED, 0L))
                        .build(),
                DashboardStatisticsResponse.OrderStatusDistributionDto.builder()
                        .status("CANCELLED")
                        .label("Đã hủy")
                        .count(statusCountsMap.getOrDefault(OrderStatus.CANCELLED, 0L))
                        .build()
        );

        // 5. Category Sales Breakdown Chart
        List<OrderDetail> allDetails = orderDetailRepository.findAll();
        Map<String, BigDecimal> categoryRevenueMap = new HashMap<>();
        Map<String, Long> categoryQtyMap = new HashMap<>();

        for (OrderDetail detail : allDetails) {
            if (detail.getMenuItem() != null && detail.getMenuItem().getCategory() != null) {
                String catName = detail.getMenuItem().getCategory().getName();
                BigDecimal lineTotal = detail.getUnitPrice().multiply(BigDecimal.valueOf(detail.getQuantity()));
                categoryRevenueMap.put(catName, categoryRevenueMap.getOrDefault(catName, BigDecimal.ZERO).add(lineTotal));
                categoryQtyMap.put(catName, categoryQtyMap.getOrDefault(catName, 0L) + detail.getQuantity());
            }
        }

        List<DashboardStatisticsResponse.CategorySalesDto> categorySales = categoryRevenueMap.entrySet().stream()
                .map(entry -> DashboardStatisticsResponse.CategorySalesDto.builder()
                        .categoryName(entry.getKey())
                        .revenue(entry.getValue())
                        .quantitySold(categoryQtyMap.getOrDefault(entry.getKey(), 0L))
                        .build())
                .sorted(Comparator.comparing(DashboardStatisticsResponse.CategorySalesDto::getRevenue).reversed())
                .collect(Collectors.toList());

        // 6. Real Recent Activities
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy");
        List<DashboardStatisticsResponse.RecentActivityDto> recentActivities = new ArrayList<>();

        // Add recent payments
        successfulPayments.stream()
                .sorted(Comparator.comparing(Payment::getPaymentTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(4)
                .forEach(p -> {
                    String timeStr = p.getPaymentTime() != null ? p.getPaymentTime().format(timeFormatter) : "Vừa xong";
                    String amountStr = p.getAmount() != null ? String.format("%,dđ", p.getAmount().longValue()) : "0đ";
                    recentActivities.add(DashboardStatisticsResponse.RecentActivityDto.builder()
                            .title("Thanh toán thành công " + amountStr)
                            .description("Mã HĐ #" + p.getId() + " - Phương thức: " + p.getPaymentMethod())
                            .timeStr(timeStr)
                            .type("SUCCESS")
                            .icon("bi-cash-coin")
                            .build());
                });

        // Add recent orders
        allOrders.stream()
                .sorted(Comparator.comparing(Order::getOrderTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(4)
                .forEach(o -> {
                    String timeStr = o.getOrderTime() != null ? o.getOrderTime().format(timeFormatter) : "Vừa xong";
                    String tableName = o.getTable() != null ? o.getTable().getTableNumber() : "Mang về";
                    recentActivities.add(DashboardStatisticsResponse.RecentActivityDto.builder()
                            .title("Đơn hàng mới tại " + tableName)
                            .description("Mã đơn #" + o.getId() + " - Trạng thái: " + o.getStatus())
                            .timeStr(timeStr)
                            .type(o.getStatus() == OrderStatus.OPEN ? "WARNING" : "INFO")
                            .icon("bi-receipt")
                            .build());
                });

        DashboardStatisticsResponse statistics = DashboardStatisticsResponse.builder()
                .revenueByDay(revenueByDay)
                .revenueByMonth(revenueByMonth)
                .revenueByYear(revenueByYear)
                .orderStatusDistribution(orderStatusDistribution)
                .categorySales(categorySales)
                .recentActivities(recentActivities)
                .build();

        return ApiResponse.success("Lấy số liệu thống kê doanh thu thành công", statistics);
    }
}

