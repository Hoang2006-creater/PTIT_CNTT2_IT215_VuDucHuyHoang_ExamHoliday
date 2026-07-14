package com.re.examholiday.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.re.examholiday.dto.request.LoginRequest;
import com.re.examholiday.dto.request.OtpRequestDto;
import com.re.examholiday.dto.request.OtpVerifyRequest;
import com.re.examholiday.dto.request.RefreshTokenRequest;
import com.re.examholiday.dto.response.ApiResponse;
import com.re.examholiday.dto.response.LoginResponse;
import com.re.examholiday.dto.response.UserProfileResponse;
import com.re.examholiday.exception.AuthenticationException;
import com.re.examholiday.exception.GlobalExceptionHandler;
import com.re.examholiday.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("POST /api/auth/login - Thành công")
    void loginSuccess() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .username("admin01")
                .password("Admin@123456")
                .build();

        LoginResponse loginResponse = LoginResponse.builder()
                .accessToken("jwt-access-token")
                .refreshToken("jwt-refresh-token")
                .tokenType("Bearer")
                .expiresIn(900)
                .role("ADMIN")
                .username("admin01")
                .build();

        when(authService.login(any(LoginRequest.class), any()))
                .thenReturn(ApiResponse.success("Đăng nhập thành công", loginResponse));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("jwt-access-token"))
                .andExpect(jsonPath("$.data.role").value("ADMIN"));
    }

    @Test
    @DisplayName("POST /api/auth/login - Username rỗng → 400")
    void loginEmptyUsername() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .username("")
                .password("Admin@123456")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/auth/login - Password rỗng → 400")
    void loginEmptyPassword() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .username("admin01")
                .password("")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/auth/login - Password quá ngắn (<8 ký tự) → 400")
    void loginPasswordTooShort() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .username("admin01")
                .password("short")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/auth/otp/request - Thành công")
    void requestOtpSuccess() throws Exception {
        OtpRequestDto request = OtpRequestDto.builder()
                .identifier("admin01")
                .build();

        when(authService.requestOtp(any(OtpRequestDto.class)))
                .thenReturn(ApiResponse.success("Mã OTP đã được gửi", null));

        mockMvc.perform(post("/api/auth/otp/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Mã OTP đã được gửi"));
    }

    @Test
    @DisplayName("POST /api/auth/otp/verify - OTP không đúng format (không phải 6 số) → 400")
    void verifyOtpInvalidFormat() throws Exception {
        OtpVerifyRequest request = OtpVerifyRequest.builder()
                .identifier("admin01")
                .otpCode("abc")
                .build();

        mockMvc.perform(post("/api/auth/otp/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/auth/otp/verify - Thành công")
    void verifyOtpSuccess() throws Exception {
        OtpVerifyRequest request = OtpVerifyRequest.builder()
                .identifier("admin01")
                .otpCode("123456")
                .build();

        LoginResponse loginResponse = LoginResponse.builder()
                .accessToken("otp-access-token")
                .refreshToken("otp-refresh-token")
                .tokenType("Bearer")
                .expiresIn(900)
                .role("ADMIN")
                .username("admin01")
                .build();

        when(authService.verifyOtp(any(OtpVerifyRequest.class), any()))
                .thenReturn(ApiResponse.success("Xác thực OTP thành công", loginResponse));

        mockMvc.perform(post("/api/auth/otp/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("otp-access-token"));
    }

    @Test
    @DisplayName("POST /api/auth/refresh - Thành công")
    void refreshTokenSuccess() throws Exception {
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken("valid-refresh-token")
                .build();

        LoginResponse loginResponse = LoginResponse.builder()
                .accessToken("new-access-token")
                .refreshToken("valid-refresh-token")
                .tokenType("Bearer")
                .expiresIn(900)
                .role("ADMIN")
                .username("admin01")
                .build();

        when(authService.refreshToken(any(RefreshTokenRequest.class)))
                .thenReturn(ApiResponse.success("Token đã được làm mới", loginResponse));

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("new-access-token"));
    }

    @Test
    @DisplayName("POST /api/auth/refresh - Refresh token rỗng → 400")
    void refreshTokenEmpty() throws Exception {
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken("")
                .build();

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/auth/login - SQL Injection attempt")
    void loginSqlInjection() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .username("admin' OR '1'='1")
                .password("anything12")
                .build();

        when(authService.login(any(LoginRequest.class), any()))
                .thenThrow(new AuthenticationException("Tên đăng nhập hoặc mật khẩu không đúng"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }
}
