package com.re.examholiday.controller;

import com.re.examholiday.dto.request.PromotionRequest;
import com.re.examholiday.dto.response.ApiResponse;
import com.re.examholiday.model.Promotion;
import com.re.examholiday.repository.PromotionRepository;
import com.re.examholiday.service.PromotionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
public class PromotionRestController {

    private final PromotionService promotionService;
    private final PromotionRepository promotionRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Promotion>>> getAllPromotions() {
        List<Promotion> promotions = promotionRepository.findAll();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách khuyến mãi thành công", promotions));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Promotion>> createPromotion(
            @Valid @RequestBody PromotionRequest request) {
        ApiResponse<Promotion> response = promotionService.createPromotion(request);
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Promotion>> updatePromotion(
            @PathVariable Integer id,
            @Valid @RequestBody PromotionRequest request) {
        ApiResponse<Promotion> response = promotionService.updatePromotion(id, request);
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePromotion(@PathVariable Integer id) {
        ApiResponse<Void> response = promotionService.deletePromotion(id);
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<Promotion>> togglePromotionStatus(@PathVariable Integer id) {
        ApiResponse<Promotion> response = promotionService.togglePromotionStatus(id);
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }
}
