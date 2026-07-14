package com.re.examholiday.controller;

import com.re.examholiday.dto.request.LoginRequest;
import com.re.examholiday.dto.request.OtpRequestDto;
import com.re.examholiday.dto.request.OtpVerifyRequest;
import com.re.examholiday.dto.request.RefreshTokenRequest;
import com.re.examholiday.dto.response.ApiResponse;
import com.re.examholiday.dto.response.LoginResponse;
import com.re.examholiday.dto.response.UserProfileResponse;
import com.re.examholiday.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/auth/login - Đăng nhập bằng Username/Password
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        ApiResponse<LoginResponse> response = authService.login(request, httpRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/auth/otp/request - Yêu cầu gửi mã OTP
     */
    @PostMapping("/otp/request")
    public ResponseEntity<ApiResponse<Object>> requestOtp(
            @Valid @RequestBody OtpRequestDto request) {
        ApiResponse<Object> response = authService.requestOtp(request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/auth/otp/verify - Xác minh OTP và đăng nhập
     */
    @PostMapping("/otp/verify")
    public ResponseEntity<ApiResponse<LoginResponse>> verifyOtp(
            @Valid @RequestBody OtpVerifyRequest request,
            HttpServletRequest httpRequest) {
        ApiResponse<LoginResponse> response = authService.verifyOtp(request, httpRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/auth/logout - Đăng xuất
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(Authentication authentication) {
        ApiResponse<Void> response = authService.logout(authentication.getName());
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/auth/refresh - Làm mới Access Token
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {
        ApiResponse<LoginResponse> response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/auth/profile - Lấy thông tin người dùng hiện tại
     */
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(
            Authentication authentication) {
        ApiResponse<UserProfileResponse> response = authService.getProfile(authentication.getName());
        return ResponseEntity.ok(response);
    }
}
