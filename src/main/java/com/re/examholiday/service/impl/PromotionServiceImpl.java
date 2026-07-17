package com.re.examholiday.service.impl;

import com.re.examholiday.dto.request.PromotionRequest;
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
import java.util.Optional;
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

    @Override
    @Transactional
    public ApiResponse<Promotion> createPromotion(PromotionRequest request) {
        if (request.getStartDate().isAfter(request.getEndDate())) {
            return ApiResponse.error("Ngày bắt đầu phải nhỏ hơn hoặc bằng ngày kết thúc");
        }
        if (promotionRepository.findByCode(request.getCode()).isPresent()) {
            return ApiResponse.error("Mã khuyến mãi đã tồn tại");
        }

        Promotion promotion = Promotion.builder()
                .code(request.getCode())
                .description(request.getDescription())
                .discountPercentage(request.getDiscountPercentage())
                .discountValue(request.getDiscountValue())
                .minOrderValue(request.getMinOrderValue())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .isActive(request.getActive())
                .build();

        Promotion saved = promotionRepository.save(promotion);
        return ApiResponse.success("Thêm khuyến mãi thành công", saved);
    }

    @Override
    @Transactional
    public ApiResponse<Promotion> updatePromotion(Integer id, PromotionRequest request) {
        Promotion promotion = promotionRepository.findById(id).orElse(null);
        if (promotion == null) {
            return ApiResponse.error("Chương trình khuyến mãi không tồn tại");
        }
        if (request.getStartDate().isAfter(request.getEndDate())) {
            return ApiResponse.error("Ngày bắt đầu phải nhỏ hơn hoặc bằng ngày kết thúc");
        }

        Optional<Promotion> existingPromo = promotionRepository.findByCode(request.getCode());
        if (existingPromo.isPresent() && !existingPromo.get().getId().equals(id)) {
            return ApiResponse.error("Mã khuyến mãi đã tồn tại");
        }

        promotion.setCode(request.getCode());
        promotion.setDescription(request.getDescription());
        promotion.setDiscountPercentage(request.getDiscountPercentage());
        promotion.setDiscountValue(request.getDiscountValue());
        promotion.setMinOrderValue(request.getMinOrderValue());
        promotion.setStartDate(request.getStartDate());
        promotion.setEndDate(request.getEndDate());
        promotion.setIsActive(request.getActive());

        Promotion saved = promotionRepository.save(promotion);
        return ApiResponse.success("Cập nhật khuyến mãi thành công", saved);
    }

    @Override
    @Transactional
    public ApiResponse<Void> deletePromotion(Integer id) {
        Promotion promotion = promotionRepository.findById(id).orElse(null);
        if (promotion == null) {
            return ApiResponse.error("Chương trình khuyến mãi không tồn tại");
        }
        promotionRepository.delete(promotion);
        return ApiResponse.success("Xóa khuyến mãi thành công");
    }

    @Override
    @Transactional
    public ApiResponse<Promotion> togglePromotionStatus(Integer id) {
        Promotion promotion = promotionRepository.findById(id).orElse(null);
        if (promotion == null) {
            return ApiResponse.error("Chương trình khuyến mãi không tồn tại");
        }
        promotion.setIsActive(!promotion.getIsActive());
        Promotion saved = promotionRepository.save(promotion);
        return ApiResponse.success("Thay đổi trạng thái khuyến mãi thành công", saved);
    }
}
