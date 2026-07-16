package com.re.examholiday.service.impl;

import com.re.examholiday.dto.response.ApiResponse;
import com.re.examholiday.model.Promotion;
import com.re.examholiday.repository.PromotionRepository;
import com.re.examholiday.service.PromotionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PromotionServiceImpl implements PromotionService {

    private final PromotionRepository promotionRepository;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<Promotion>> getActivePromotions() {
        LocalDate today = LocalDate.now();
        List<Promotion> activeList = promotionRepository.findAll().stream()
                .filter(p -> Boolean.TRUE.equals(p.getIsActive()) &&
                             !p.getStartDate().isAfter(today) &&
                             !p.getEndDate().isBefore(today))
                .collect(Collectors.toList());
        return ApiResponse.success("Lấy danh sách khuyến mãi đang hoạt động thành công", activeList);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<Promotion> getPromotionDetail(Integer id) {
        Promotion promotion = promotionRepository.findById(id).orElse(null);
        if (promotion == null) {
            return ApiResponse.error("Chương trình khuyến mãi không tồn tại");
        }
        return ApiResponse.success("Lấy chi tiết khuyến mãi thành công", promotion);
    }
}
