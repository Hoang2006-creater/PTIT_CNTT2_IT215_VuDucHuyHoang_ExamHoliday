package com.re.examholiday.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserProfileResponse {

    private Long id;
    private String username;
    private String email;
    private String phone;
    private String role;
    private String status;
    private LocalDateTime createdAt;

    // Customer specific fields
    private String fullName;
    private Integer loyaltyPoints;
    private String membershipClass;
}
