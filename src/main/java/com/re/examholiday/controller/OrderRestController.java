package com.re.examholiday.controller;

import com.re.examholiday.dto.response.ApiResponse;
import com.re.examholiday.model.Order;
import com.re.examholiday.model.enumeration.OrderStatus;
import com.re.examholiday.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import com.re.examholiday.model.OrderDetail;
import com.re.examholiday.model.enumeration.OrderItemStatus;
import com.re.examholiday.repository.OrderDetailRepository;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderRestController {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOrders(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "orderTime") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction
    ) {
        List<Order> orders = orderRepository.findAll();

        // 1. Filtering
        if (status != null) {
            orders = orders.stream()
                    .filter(o -> o.getStatus() == status)
                    .collect(Collectors.toList());
        }

        if (employeeId != null) {
            orders = orders.stream()
                    .filter(o -> o.getEmployee() != null && o.getEmployee().getId().equals(employeeId))
                    .collect(Collectors.toList());
        }

        if (search != null && !search.trim().isEmpty()) {
            String cleanSearch = search.trim().toLowerCase();
            orders = orders.stream()
                    .filter(o -> 
                        String.valueOf(o.getId()).contains(cleanSearch) ||
                        (o.getCustomer() != null && o.getCustomer().getFullName().toLowerCase().contains(cleanSearch)) ||
                        (o.getTable() != null && o.getTable().getTableNumber().toLowerCase().contains(cleanSearch))
                    )
                    .collect(Collectors.toList());
        }

        if (startDate != null) {
            LocalDateTime startLdt = startDate.atStartOfDay();
            orders = orders.stream()
                    .filter(o -> o.getOrderTime() != null && !o.getOrderTime().isBefore(startLdt))
                    .collect(Collectors.toList());
        }

        if (endDate != null) {
            LocalDateTime endLdt = endDate.atTime(LocalTime.MAX);
            orders = orders.stream()
                    .filter(o -> o.getOrderTime() != null && !o.getOrderTime().isAfter(endLdt))
                    .collect(Collectors.toList());
        }

        // 2. Sorting
        Comparator<Order> comparator = null;
        if ("orderTime".equals(sortBy)) {
            comparator = Comparator.comparing(Order::getOrderTime, Comparator.nullsLast(Comparator.naturalOrder()));
        } else if ("totalAmount".equals(sortBy)) {
            comparator = Comparator.comparing(Order::getTotalAmount, Comparator.nullsLast(Comparator.naturalOrder()));
        } else if ("id".equals(sortBy)) {
            comparator = Comparator.comparing(Order::getId, Comparator.nullsLast(Comparator.naturalOrder()));
        }

        if (comparator != null) {
            if ("DESC".equalsIgnoreCase(direction)) {
                comparator = comparator.reversed();
            }
            orders.sort(comparator);
        }

        // 3. Pagination
        int totalElements = orders.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, totalElements);
        
        List<Order> content = new ArrayList<>();
        if (fromIndex < totalElements) {
            content = orders.subList(fromIndex, toIndex);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("content", content);
        data.put("totalElements", totalElements);
        data.put("totalPages", totalPages);
        data.put("pageNumber", page);
        data.put("pageSize", size);

        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách đơn hàng thành công", data));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Order>> getOrderDetail(@PathVariable Long id) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Không tìm thấy đơn hàng"));
        }
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết đơn hàng thành công", order));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Order>> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Không tìm thấy đơn hàng"));
        }
        order.setStatus(status);
        Order saved = orderRepository.save(order);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái đơn hàng thành công", saved));
    }

    @PutMapping("/details/{detailId}/status")
    public ResponseEntity<ApiResponse<OrderDetail>> updateOrderDetailStatus(
            @PathVariable Long detailId,
            @RequestParam OrderItemStatus status) {
        OrderDetail orderDetail = orderDetailRepository.findById(detailId).orElse(null);
        if (orderDetail == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Không tìm thấy món trong đơn hàng"));
        }
        orderDetail.setStatus(status);
        OrderDetail saved = orderDetailRepository.save(orderDetail);

        // Auto transition Order to COMPLETED if all order items are SERVED (or CANCELLED)
        Order order = orderDetail.getOrder();
        if (order != null) {
            List<OrderDetail> allDetails = orderDetailRepository.findByOrderId(order.getId());
            if (allDetails != null && !allDetails.isEmpty()) {
                boolean allServedOrCancelled = allDetails.stream()
                        .allMatch(d -> d.getStatus() == OrderItemStatus.SERVED || d.getStatus() == OrderItemStatus.CANCELLED);
                boolean hasServed = allDetails.stream()
                        .anyMatch(d -> d.getStatus() == OrderItemStatus.SERVED);
                
                if (allServedOrCancelled && hasServed) {
                    order.setStatus(OrderStatus.COMPLETED);
                    orderRepository.save(order);
                }
            }
        }

        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái món thành công", saved));
    }
}
