package com.re.examholiday.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateEmployeeRequest {

    @NotBlank(message = "Họ và tên không được để trống")
    @Size(max = 100, message = "Họ và tên không được vượt quá 100 ký tự")
    private String fullName;

    @NotBlank(message = "Vị trí công việc không được để trống")
    @Size(max = 50, message = "Vị trí không được vượt quá 50 ký tự")
    private String position;

    @NotNull(message = "Lương không được để trống")
    @PositiveOrZero(message = "Lương phải lớn hơn hoặc bằng 0")
    private BigDecimal salary;

    @NotNull(message = "Ngày vào làm không được để trống")
    private LocalDate hireDate;

    @NotBlank(message = "Trạng thái làm việc không được để trống")
    private String status;
}
