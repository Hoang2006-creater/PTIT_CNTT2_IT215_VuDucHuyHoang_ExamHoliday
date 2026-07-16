package com.re.examholiday.service.impl;

import com.re.examholiday.dto.request.CartItem;
import com.re.examholiday.dto.request.CartItemRequest;
import com.re.examholiday.dto.request.CheckoutRequest;
import com.re.examholiday.dto.response.ApiResponse;
import com.re.examholiday.dto.response.CartItemResponse;
import com.re.examholiday.dto.response.CartSummaryResponse;
import com.re.examholiday.model.*;
import com.re.examholiday.model.enumeration.OrderItemStatus;
import com.re.examholiday.model.enumeration.OrderStatus;
import com.re.examholiday.repository.*;
import com.re.examholiday.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final MenuItemRepository menuItemRepository;
    private final RestaurantTableRepository restaurantTableRepository;
    private final EmployeeRepository employeeRepository;
    private final OrderRepository orderRepository;

    private final Map<String, List<CartItem>> userCarts = new ConcurrentHashMap<>();

    @Override
    public ApiResponse<CartSummaryResponse> addToCart(String username, CartItemRequest request) {
        MenuItem menuItem = menuItemRepository.findById(request.getMenuItemId()).orElse(null);
        if (menuItem == null || !Boolean.TRUE.equals(menuItem.getIsAvailable())) {
            return ApiResponse.error("Món ăn không tồn tại hoặc đã ngừng bán");
        }

        List<CartItem> cart = userCarts.computeIfAbsent(username, k -> new ArrayList<>());

        // Check if item already in cart
        Optional<CartItem> existing = cart.stream()
                .filter(item -> item.getMenuItemId().equals(request.getMenuItemId()))
                .findFirst();

        if (existing.isPresent()) {
            CartItem item = existing.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            if (request.getNotes() != null) {
                item.setNotes(request.getNotes());
            }
        } else {
            cart.add(CartItem.builder()
                    .menuItemId(request.getMenuItemId())
                    .quantity(request.getQuantity())
                    .notes(request.getNotes())
                    .build());
        }

        return getCart(username);
    }

    @Override
    public ApiResponse<CartSummaryResponse> updateCartItem(String username, CartItemRequest request) {
        List<CartItem> cart = userCarts.get(username);
        if (cart == null) {
            return ApiResponse.error("Giỏ hàng trống");
        }

        Optional<CartItem> existing = cart.stream()
                .filter(item -> item.getMenuItemId().equals(request.getMenuItemId()))
                .findFirst();

        if (existing.isPresent()) {
            CartItem item = existing.get();
            item.setQuantity(request.getQuantity());
            item.setNotes(request.getNotes());
        } else {
            return ApiResponse.error("Không tìm thấy món ăn trong giỏ hàng");
        }

        return getCart(username);
    }

    @Override
    public ApiResponse<CartSummaryResponse> removeFromCart(String username, Long menuItemId) {
        List<CartItem> cart = userCarts.get(username);
        if (cart != null) {
            cart.removeIf(item -> item.getMenuItemId().equals(menuItemId));
        }
        return getCart(username);
    }

    @Override
    public ApiResponse<CartSummaryResponse> getCart(String username) {
        List<CartItem> cartItems = userCarts.getOrDefault(username, Collections.emptyList());
        List<CartItemResponse> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (CartItem item : cartItems) {
            MenuItem menuItem = menuItemRepository.findById(item.getMenuItemId()).orElse(null);
            if (menuItem != null && Boolean.TRUE.equals(menuItem.getIsAvailable())) {
                BigDecimal subTotal = menuItem.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                total = total.add(subTotal);

                items.add(CartItemResponse.builder()
                        .menuItemId(menuItem.getId())
                        .menuItemName(menuItem.getName())
                        .imageUrl(menuItem.getImageUrl())
                        .quantity(item.getQuantity())
                        .unitPrice(menuItem.getPrice())
                        .subTotal(subTotal)
                        .notes(item.getNotes())
                        .build());
            }
        }

        CartSummaryResponse summary = CartSummaryResponse.builder()
                .items(items)
                .totalAmount(total)
                .build();

        return ApiResponse.success("Lấy giỏ hàng thành công", summary);
    }

    @Override
    @Transactional
    public ApiResponse<Order> checkout(String username, CheckoutRequest request) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return ApiResponse.error("Không tìm thấy tài khoản");
        }

        Customer customer = customerRepository.findByUserId(user.getId()).orElse(null);
        if (customer == null) {
            return ApiResponse
                    .error("Tài khoản chưa được liên kết với thông tin khách hàng. Vui lòng thiết lập hồ sơ trước.");
        }

        List<CartItem> cartItems = userCarts.get(username);
        if (cartItems == null || cartItems.isEmpty()) {
            return ApiResponse.error("Giỏ hàng của bạn đang trống");
        }

        // Table Selection
        RestaurantTable table = null;
        if (request != null && request.getTableId() != null) {
            table = restaurantTableRepository.findById(Math.toIntExact(request.getTableId())).orElse(null);
        }
        if (table == null) {
            List<RestaurantTable> tables = restaurantTableRepository.findAll();
            if (!tables.isEmpty()) {
                table = tables.get(0);
            } else {
                return ApiResponse.error("Không có bàn ăn nào trong hệ thống để đặt món");
            }
        }

        // Employee Assignment
        Employee employee = null;
        List<Employee> employees = employeeRepository.findAll();
        if (!employees.isEmpty()) {
            employee = employees.get(0);
        } else {
            return ApiResponse.error("Không có nhân viên nào trong hệ thống để gán đơn");
        }

        // Build Order
        Order order = Order.builder()
                .table(table)
                .customer(customer)
                .employee(employee)
                .orderTime(LocalDateTime.now())
                .status(OrderStatus.OPEN)
                .totalAmount(BigDecimal.ZERO)
                .orderDetails(new ArrayList<>())
                .build();

        BigDecimal total = BigDecimal.ZERO;
        List<OrderDetail> details = new ArrayList<>();

        for (CartItem item : cartItems) {
            MenuItem menuItem = menuItemRepository.findById(item.getMenuItemId()).orElse(null);
            if (menuItem == null || !Boolean.TRUE.equals(menuItem.getIsAvailable())) {
                return ApiResponse.error("Món ăn '" + (menuItem != null ? menuItem.getName() : item.getMenuItemId())
                        + "' không còn phục vụ. Vui lòng cập nhật giỏ hàng.");
            }

            BigDecimal subTotal = menuItem.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            total = total.add(subTotal);

            details.add(OrderDetail.builder()
                    .order(order)
                    .menuItem(menuItem)
                    .quantity(item.getQuantity())
                    .unitPrice(menuItem.getPrice())
                    .notes(item.getNotes())
                    .status(OrderItemStatus.ORDERED)
                    .build());
        }

        order.setTotalAmount(total);
        order.setOrderDetails(details);

        Order savedOrder = orderRepository.save(order);

        // Clear cart
        userCarts.remove(username);

        log.info("Đặt món thành công: username='{}', orderId='{}', totalAmount='{}'", username, savedOrder.getId(),
                total);
        return ApiResponse.success("Xác nhận đặt món thành công", savedOrder);
    }
}
