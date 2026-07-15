package com.re.examholiday.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateSalaryRequest {

    @NotNull(message = "Lương không được để trống")
    @PositiveOrZero(message = "Lương phải lớn hơn hoặc bằng 0")
    private BigDecimal salary;
}
