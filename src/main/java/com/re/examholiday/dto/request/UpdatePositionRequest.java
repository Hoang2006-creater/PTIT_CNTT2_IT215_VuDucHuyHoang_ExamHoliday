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
public class UpdatePositionRequest {

    @NotBlank(message = "Vị trí công việc không được để trống")
    @Size(max = 50, message = "Vị trí không được vượt quá 50 ký tự")
    private String position;
}
