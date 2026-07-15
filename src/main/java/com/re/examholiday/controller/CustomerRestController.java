package com.re.examholiday.controller;

import com.re.examholiday.dto.request.CreateCustomerRequest;
import com.re.examholiday.dto.request.UpdateCustomerRequest;
import com.re.examholiday.dto.request.UpdateLoyaltyPointsRequest;
import com.re.examholiday.dto.response.ApiResponse;
import com.re.examholiday.dto.response.CustomerOrderResponse;
import com.re.examholiday.dto.response.CustomerResponse;
import com.re.examholiday.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class CustomerRestController {

    private final CustomerService customerService;

    /**
     * GET /api/customers - Xem danh sách khách hàng
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomerResponse>>> getAllCustomers() {
        ApiResponse<List<CustomerResponse>> response = customerService.getAllCustomers();
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/customers/{id} - Xem chi tiết khách hàng
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomerDetail(@PathVariable Long id) {
        ApiResponse<CustomerResponse> response = customerService.getCustomerDetail(id);
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/customers - Thêm khách hàng mới
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CustomerResponse>> createCustomer(
            @Valid @RequestBody CreateCustomerRequest request) {
        ApiResponse<CustomerResponse> response = customerService.createCustomer(request);
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/customers/{id} - Cập nhật khách hàng
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCustomerRequest request) {
        ApiResponse<CustomerResponse> response = customerService.updateCustomer(id, request);
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/customers/{id} - Xóa khách hàng
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCustomer(@PathVariable Long id) {
        ApiResponse<Void> response = customerService.deleteCustomer(id);
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/customers/{id}/points - Quản lý điểm tích lũy (Cập nhật điểm)
     */
    @PutMapping("/{id}/points")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateLoyaltyPoints(
            @PathVariable Long id,
            @Valid @RequestBody UpdateLoyaltyPointsRequest request) {
        ApiResponse<CustomerResponse> response = customerService.updateLoyaltyPoints(id, request);
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/customers/{id}/orders - Xem lịch sử gọi món
     */
    @GetMapping("/{id}/orders")
    public ResponseEntity<ApiResponse<List<CustomerOrderResponse>>> getCustomerOrderHistory(
            @PathVariable Long id) {
        ApiResponse<List<CustomerOrderResponse>> response = customerService.getCustomerOrderHistory(id);
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }
}
