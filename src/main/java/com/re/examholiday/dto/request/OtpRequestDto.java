package com.re.examholiday.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpRequestDto {

    @NotBlank(message = "Thông tin đăng nhập không được để trống")
    @Size(max = 100, message = "Thông tin đăng nhập không được quá 100 ký tự")
    private String identifier;
}
