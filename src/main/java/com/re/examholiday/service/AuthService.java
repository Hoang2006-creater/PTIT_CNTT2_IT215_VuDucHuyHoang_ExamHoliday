package com.re.examholiday.service;

import com.re.examholiday.dto.request.LoginRequest;
import com.re.examholiday.dto.request.OtpRequestDto;
import com.re.examholiday.dto.request.OtpVerifyRequest;
import com.re.examholiday.dto.request.RefreshTokenRequest;
import com.re.examholiday.dto.response.ApiResponse;
import com.re.examholiday.dto.response.LoginResponse;
import com.re.examholiday.dto.response.UserProfileResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {

    ApiResponse<LoginResponse> login(LoginRequest request, HttpServletRequest httpRequest);

    ApiResponse<Object> requestOtp(OtpRequestDto request);

    ApiResponse<LoginResponse> verifyOtp(OtpVerifyRequest request, HttpServletRequest httpRequest);

    ApiResponse<Void> logout(String username);

    ApiResponse<LoginResponse> refreshToken(RefreshTokenRequest request);

    ApiResponse<UserProfileResponse> getProfile(String username);
}
