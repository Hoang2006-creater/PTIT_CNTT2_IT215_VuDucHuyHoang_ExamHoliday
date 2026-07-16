package com.re.examholiday.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoyaltyPointHistoryResponse {
    private Long orderId;
    private LocalDateTime transactionTime;
    private String description;
    private Integer pointsChange;
    private String type; // "EARNED", "SPENT" etc.
}
