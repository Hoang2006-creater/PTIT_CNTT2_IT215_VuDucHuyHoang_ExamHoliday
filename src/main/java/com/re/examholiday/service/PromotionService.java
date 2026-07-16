package com.re.examholiday.service;

import com.re.examholiday.dto.response.ApiResponse;
import com.re.examholiday.model.Promotion;
import java.util.List;

public interface PromotionService {
    ApiResponse<List<Promotion>> getActivePromotions();
    ApiResponse<Promotion> getPromotionDetail(Integer id);
}
