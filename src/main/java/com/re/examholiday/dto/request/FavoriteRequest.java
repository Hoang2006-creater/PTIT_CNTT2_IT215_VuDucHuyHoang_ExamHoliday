package com.re.examholiday.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FavoriteRequest {
    @NotNull(message = "ID món ăn không được để trống")
    private Long menuItemId;
}
