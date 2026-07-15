package com.re.examholiday.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MostUsedTableResponse {
    private Integer tableId;
    private String tableNumber;
    private Integer capacity;
    private Long orderCount;
}
