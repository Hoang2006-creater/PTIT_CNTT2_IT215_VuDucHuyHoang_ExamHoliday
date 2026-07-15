package com.re.examholiday.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateLoyaltyPointsRequest {

    @NotNull(message = "Điểm tích lũy không được để trống")
    @PositiveOrZero(message = "Điểm tích lũy phải lớn hơn hoặc bằng 0")
    private Integer loyaltyPoints;
}
