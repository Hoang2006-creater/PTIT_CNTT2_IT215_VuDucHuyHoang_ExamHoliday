package com.re.examholiday.controller;

import com.re.examholiday.dto.response.ApiResponse;
import com.re.examholiday.dto.response.CustomerResponse;
import com.re.examholiday.dto.response.LoyaltyPointHistoryResponse;
import com.re.examholiday.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/customer/loyalty")
@RequiredArgsConstructor
public class CustomerLoyaltyRestController {

    private final CustomerService customerService;

    /**
     * GET /api/customer/loyalty - Xem điểm tích lũy và hạng thành viên
     */
    @GetMapping
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomerLoyaltyInfo(Authentication authentication) {
        ApiResponse<CustomerResponse> response = customerService.getCustomerLoyaltyInfo(authentication.getName());
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/customer/loyalty/history - Xem lịch sử tích lũy điểm từ hóa đơn
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<LoyaltyPointHistoryResponse>>> getLoyaltyPointHistory(Authentication authentication) {
        ApiResponse<List<LoyaltyPointHistoryResponse>> response = customerService.getLoyaltyPointHistory(authentication.getName());
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }
}
