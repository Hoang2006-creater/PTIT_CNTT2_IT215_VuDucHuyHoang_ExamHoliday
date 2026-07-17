package com.re.examholiday.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PromotionRequest {
    @NotBlank(message = "Mã khuyến mãi không được để trống")
    @Size(max = 50, message = "Mã khuyến mãi tối đa 50 ký tự")
    private String code;

    @Size(max = 255, message = "Mô tả tối đa 255 ký tự")
    private String description;

    @Min(value = 1, message = "Phần trăm giảm giá phải từ 1 đến 100")
    @Max(value = 100, message = "Phần trăm giảm giá phải từ 1 đến 100")
    private Integer discountPercentage;

    @PositiveOrZero(message = "Giá trị giảm giá không được âm")
    private BigDecimal discountValue;

    @NotNull(message = "Đơn hàng tối thiểu không được để trống")
    @PositiveOrZero(message = "Đơn hàng tối thiểu không được âm")
    private BigDecimal minOrderValue = BigDecimal.ZERO;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDate startDate;

    @NotNull(message = "Ngày kết thúc không được để trống")
    private LocalDate endDate;

    @NotNull(message = "Trạng thái hoạt động không được để trống")
    private Boolean active;
}
