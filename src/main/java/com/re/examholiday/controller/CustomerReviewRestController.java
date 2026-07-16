package com.re.examholiday.controller;

import com.re.examholiday.dto.request.CreateReviewRequest;
import com.re.examholiday.dto.request.UpdateReviewRequest;
import com.re.examholiday.dto.response.ApiResponse;
import com.re.examholiday.model.Review;
import com.re.examholiday.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer/reviews")
@RequiredArgsConstructor
public class CustomerReviewRestController {

    private final ReviewService reviewService;

    /**
     * POST /api/customer/reviews - Gửi đánh giá hóa đơn đã hoàn thành
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Review>> createReview(
            Authentication authentication,
            @Valid @RequestBody CreateReviewRequest request) {
        ApiResponse<Review> response = reviewService.createReview(authentication.getName(), request);
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/customer/reviews/{id} - Chỉnh sửa nội dung và điểm đánh giá
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Review>> updateReview(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody UpdateReviewRequest request) {
        ApiResponse<Review> response = reviewService.updateReview(authentication.getName(), id, request);
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/customer/reviews/{id} - Xóa đánh giá
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            Authentication authentication,
            @PathVariable Long id) {
        ApiResponse<Void> response = reviewService.deleteReview(authentication.getName(), id);
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/customer/reviews/me - Lấy danh sách đánh giá của chính mình
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<Review>>> getMyReviews(Authentication authentication) {
        ApiResponse<List<Review>> response = reviewService.getMyReviews(authentication.getName());
        return ResponseEntity.ok(response);
    }
}
