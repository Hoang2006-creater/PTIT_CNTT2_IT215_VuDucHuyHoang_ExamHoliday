package com.re.examholiday.controller;

import com.re.examholiday.dto.request.CartItemRequest;
import com.re.examholiday.dto.request.CheckoutRequest;
import com.re.examholiday.dto.response.ApiResponse;
import com.re.examholiday.dto.response.CartSummaryResponse;
import com.re.examholiday.model.Order;
import com.re.examholiday.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer/cart")
@RequiredArgsConstructor
public class CustomerCartRestController {

    private final CartService cartService;

    /**
     * GET /api/customer/cart - Xem chi tiết giỏ hàng & Tính tổng tiền
     */
    @GetMapping
    public ResponseEntity<ApiResponse<CartSummaryResponse>> getCart(Authentication authentication) {
        ApiResponse<CartSummaryResponse> response = cartService.getCart(authentication.getName());
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/customer/cart/add - Thêm món vào giỏ hàng
     */
    @PostMapping("/add")
    public ResponseEntity<ApiResponse<CartSummaryResponse>> addToCart(
            Authentication authentication,
            @Valid @RequestBody CartItemRequest request) {
        ApiResponse<CartSummaryResponse> response = cartService.addToCart(authentication.getName(), request);
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/customer/cart/update - Cập nhật số lượng & Ghi chú món ăn trong giỏ
     */
    @PutMapping("/update")
    public ResponseEntity<ApiResponse<CartSummaryResponse>> updateCartItem(
            Authentication authentication,
            @Valid @RequestBody CartItemRequest request) {
        ApiResponse<CartSummaryResponse> response = cartService.updateCartItem(authentication.getName(), request);
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/customer/cart/remove/{menuItemId} - Xóa món khỏi giỏ hàng
     */
    @DeleteMapping("/remove/{menuItemId}")
    public ResponseEntity<ApiResponse<CartSummaryResponse>> removeFromCart(
            Authentication authentication,
            @PathVariable Long menuItemId) {
        ApiResponse<CartSummaryResponse> response = cartService.removeFromCart(authentication.getName(), menuItemId);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/customer/cart/checkout - Xác nhận đặt món và tạo Order/OrderDetail
     */
    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<Order>> checkout(
            Authentication authentication,
            @RequestBody(required = false) CheckoutRequest request) {
        ApiResponse<Order> response = cartService.checkout(authentication.getName(), request);
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }
}
