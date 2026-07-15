package com.re.examholiday.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerResponse {
    private Long id;
    private String fullName;
    private String phone;
    private Integer loyaltyPoints;
    private String membershipClass;
    private Long userId;
    private String username;
    private String email;
    private java.math.BigDecimal totalSpent;
}
