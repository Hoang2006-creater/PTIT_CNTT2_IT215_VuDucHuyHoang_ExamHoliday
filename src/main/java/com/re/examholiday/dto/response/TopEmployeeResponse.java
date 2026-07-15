package com.re.examholiday.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopEmployeeResponse {
    private Long employeeId;
    private String fullName;
    private String position;
    private Long orderCount;
}
