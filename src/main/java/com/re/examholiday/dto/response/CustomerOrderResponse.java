package com.re.examholiday.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerOrderResponse {
    private Long id;
    private LocalDateTime orderTime;
    private BigDecimal totalAmount;
    private String status;
    private String tableNumber;
    private String employeeName;
}
