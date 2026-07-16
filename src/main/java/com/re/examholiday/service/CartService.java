package com.re.examholiday.service;

import com.re.examholiday.dto.request.CartItemRequest;
import com.re.examholiday.dto.request.CheckoutRequest;
import com.re.examholiday.dto.response.ApiResponse;
import com.re.examholiday.dto.response.CartSummaryResponse;
import com.re.examholiday.model.Order;

public interface CartService {
    ApiResponse<CartSummaryResponse> addToCart(String username, CartItemRequest request);
    ApiResponse<CartSummaryResponse> updateCartItem(String username, CartItemRequest request);
    ApiResponse<CartSummaryResponse> removeFromCart(String username, Long menuItemId);
    ApiResponse<CartSummaryResponse> getCart(String username);
    ApiResponse<Order> checkout(String username, CheckoutRequest request);
}
