package com.re.examholiday.controller;

import com.re.examholiday.dto.response.ApiResponse;
import com.re.examholiday.model.Promotion;
import com.re.examholiday.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
public class CustomerPromotionRestController {

    private final PromotionService promotionService;

    /**
     * GET /api/customer/promotions - Danh sách khuyến mãi đang hoạt động
     */
    @GetMapping("/promotions")
    public ResponseEntity<ApiResponse<List<Promotion>>> getActivePromotions() {
        ApiResponse<List<Promotion>> response = promotionService.getActivePromotions();
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/customer/vouchers - Danh sách Voucher đang hoạt động (đồng nhất khuyến mãi có mã)
     */
    @GetMapping("/vouchers")
    public ResponseEntity<ApiResponse<List<Promotion>>> getActiveVouchers() {
        ApiResponse<List<Promotion>> response = promotionService.getActivePromotions();
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/customer/promotions/{id} - Chi tiết khuyến mãi theo ID
     */
    @GetMapping("/promotions/{id}")
    public ResponseEntity<ApiResponse<Promotion>> getPromotionDetail(@PathVariable Integer id) {
        ApiResponse<Promotion> response = promotionService.getPromotionDetail(id);
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }
}
