package com.re.examholiday.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BestSellingItemResponse {
    private Long menuItemId;
    private String name;
    private BigDecimal price;
    private Long totalSold;
}
