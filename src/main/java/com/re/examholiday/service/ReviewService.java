package com.re.examholiday.service;

import com.re.examholiday.dto.request.CreateReviewRequest;
import com.re.examholiday.dto.request.UpdateReviewRequest;
import com.re.examholiday.dto.response.ApiResponse;
import com.re.examholiday.model.Review;

import java.util.List;

public interface ReviewService {
    ApiResponse<Review> createReview(String username, CreateReviewRequest request);
    ApiResponse<Review> updateReview(String username, Long reviewId, UpdateReviewRequest request);
    ApiResponse<Void> deleteReview(String username, Long reviewId);
    ApiResponse<List<Review>> getMyReviews(String username);
}
