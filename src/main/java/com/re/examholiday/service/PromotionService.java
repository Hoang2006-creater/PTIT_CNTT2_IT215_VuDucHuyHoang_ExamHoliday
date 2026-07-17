package com.re.examholiday.service;

import com.re.examholiday.dto.request.PromotionRequest;
import com.re.examholiday.dto.response.ApiResponse;
import com.re.examholiday.model.Promotion;
import java.util.List;

public interface PromotionService {
    ApiResponse<List<Promotion>> getActivePromotions();
    ApiResponse<Promotion> getPromotionDetail(Integer id);
    ApiResponse<Promotion> createPromotion(PromotionRequest request);
    ApiResponse<Promotion> updatePromotion(Integer id, PromotionRequest request);
    ApiResponse<Void> deletePromotion(Integer id);
    ApiResponse<Promotion> togglePromotionStatus(Integer id);
}
