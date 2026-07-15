package com.re.examholiday.dto.response;

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
public class EmployeeResponse {
    private Long id;
    private String fullName;
    private String position;
    private BigDecimal salary;
    private LocalDate hireDate;
    private String status;
    private Long userId;
    private String username;
}
