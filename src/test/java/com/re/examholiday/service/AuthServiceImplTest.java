package com.re.examholiday.service;

import com.re.examholiday.dto.request.LoginRequest;
import com.re.examholiday.dto.request.OtpRequestDto;
import com.re.examholiday.dto.request.OtpVerifyRequest;
import com.re.examholiday.dto.request.RefreshTokenRequest;
import com.re.examholiday.dto.response.ApiResponse;
import com.re.examholiday.dto.response.LoginResponse;
import com.re.examholiday.dto.response.UserProfileResponse;
import com.re.examholiday.exception.AccountLockedException;
import com.re.examholiday.exception.AuthenticationException;
import com.re.examholiday.exception.InvalidOtpException;
import com.re.examholiday.exception.TokenExpiredException;
import com.re.examholiday.model.*;
import com.re.examholiday.model.enumeration.LoginMethod;
import com.re.examholiday.model.enumeration.RoleName;
import com.re.examholiday.model.enumeration.UserStatus;
import com.re.examholiday.repository.LoginHistoryRepository;
import com.re.examholiday.repository.OtpTokenRepository;
import com.re.examholiday.repository.RefreshTokenRepository;
import com.re.examholiday.repository.UserRepository;
import com.re.examholiday.service.impl.AuthServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private OtpTokenRepository otpTokenRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private LoginHistoryRepository loginHistoryRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        // Set config values
        ReflectionTestUtils.setField(authService, "maxFailedAttempts", 5);
        ReflectionTestUtils.setField(authService, "lockDurationMinutes", 30);
        ReflectionTestUtils.setField(authService, "otpExpirationMinutes", 5);
        ReflectionTestUtils.setField(authService, "otpMaxRequestsPerWindow", 3);

        // Create test role
        adminRole = Role.builder()
                .id(1)
                .name(RoleName.ADMIN)
                .description("Administrator")
                .build();

        // Create test user
        testUser = User.builder()
                .id(1L)
                .username("admin01")
                .passwordHash("$2a$12$encodedPasswordHash")
                .email("admin@restaurant.com")
                .phone("0901234567")
                .role(adminRole)
                .status(UserStatus.ACTIVE)
                .failedLoginAttempts(0)
                .lockedUntil(null)
                .build();

        // Mock HttpServletRequest
        lenient().when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn(null);
        lenient().when(httpServletRequest.getHeader("X-Real-IP")).thenReturn(null);
        lenient().when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        lenient().when(httpServletRequest.getHeader("User-Agent")).thenReturn("JUnit Test Agent");
    }

    // ==================== LOGIN TESTS ====================

    @Nested
    @DisplayName("UC-LOGIN-01: Đăng nhập Username/Password")
    class LoginTests {

        @Test
        @DisplayName("TC-LOGIN-01: Đăng nhập thành công với thông tin đúng")
        void loginSuccess() {
            LoginRequest request = LoginRequest.builder()
                    .username("admin01")
                    .password("Admin@123456")
                    .build();

            when(userRepository.findByUsername("admin01")).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("Admin@123456", testUser.getPasswordHash())).thenReturn(true);
            when(jwtService.generateAccessToken(testUser)).thenReturn("access-token-xxx");
            when(jwtService.generateRefreshTokenValue()).thenReturn("refresh-token-xxx");
            when(jwtService.getAccessTokenExpirationMs()).thenReturn(900000L);
            when(jwtService.getRefreshTokenExpirationMs()).thenReturn(604800000L);
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));
            when(loginHistoryRepository.save(any(LoginHistory.class))).thenAnswer(i -> i.getArgument(0));
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

            ApiResponse<LoginResponse> response = authService.login(request, httpServletRequest);

            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getMessage()).isEqualTo("Đăng nhập thành công");
            assertThat(response.getData().getAccessToken()).isEqualTo("access-token-xxx");
            assertThat(response.getData().getRefreshToken()).isEqualTo("refresh-token-xxx");
            assertThat(response.getData().getRole()).isEqualTo("ADMIN");
            assertThat(response.getData().getUsername()).isEqualTo("admin01");

            verify(loginHistoryRepository).save(any(LoginHistory.class));
            verify(refreshTokenRepository).revokeAllByUserId(testUser.getId());
        }

        @Test
        @DisplayName("TC-LOGIN-06: Đăng nhập với username không tồn tại")
        void loginUsernameNotFound() {
            LoginRequest request = LoginRequest.builder()
                    .username("nonexistent")
                    .password("Any@12345")
                    .build();

            when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.login(request, httpServletRequest))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessage("Tên đăng nhập hoặc mật khẩu không đúng");
        }

        @Test
        @DisplayName("TC-LOGIN-07: Đăng nhập với password sai")
        void loginWrongPassword() {
            LoginRequest request = LoginRequest.builder()
                    .username("admin01")
                    .password("WrongPass1")
                    .build();

            when(userRepository.findByUsername("admin01")).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("WrongPass1", testUser.getPasswordHash())).thenReturn(false);
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
            when(loginHistoryRepository.save(any(LoginHistory.class))).thenAnswer(i -> i.getArgument(0));

            assertThatThrownBy(() -> authService.login(request, httpServletRequest))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessageContaining("Còn 4 lần thử");

            assertThat(testUser.getFailedLoginAttempts()).isEqualTo(1);
        }

        @Test
        @DisplayName("TC-LOGIN-08: Đăng nhập khi tài khoản bị BLOCKED")
        void loginAccountBlocked() {
            testUser.setStatus(UserStatus.BLOCKED);

            LoginRequest request = LoginRequest.builder()
                    .username("admin01")
                    .password("Admin@123456")
                    .build();

            when(userRepository.findByUsername("admin01")).thenReturn(Optional.of(testUser));
            when(loginHistoryRepository.save(any(LoginHistory.class))).thenAnswer(i -> i.getArgument(0));

            assertThatThrownBy(() -> authService.login(request, httpServletRequest))
                    .isInstanceOf(AccountLockedException.class)
                    .hasMessageContaining("Tài khoản đã bị khóa");
        }

        @Test
        @DisplayName("TC-BV-03: Đăng nhập sai 5 lần → bị khóa tạm thời")
        void loginFifthFailedAttemptLocksAccount() {
            testUser.setFailedLoginAttempts(4); // đã sai 4 lần trước đó

            LoginRequest request = LoginRequest.builder()
                    .username("admin01")
                    .password("WrongPass5")
                    .build();

            when(userRepository.findByUsername("admin01")).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("WrongPass5", testUser.getPasswordHash())).thenReturn(false);
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
            when(loginHistoryRepository.save(any(LoginHistory.class))).thenAnswer(i -> i.getArgument(0));

            assertThatThrownBy(() -> authService.login(request, httpServletRequest))
                    .isInstanceOf(AccountLockedException.class)
                    .hasMessageContaining("bị khóa");

            assertThat(testUser.getFailedLoginAttempts()).isEqualTo(5);
            assertThat(testUser.getLockedUntil()).isNotNull();
            assertThat(testUser.getLockedUntil()).isAfter(LocalDateTime.now());
        }

        @Test
        @DisplayName("TC-LOGIN-04: Đăng nhập thành công sau khi hết thời gian khóa tạm")
        void loginSuccessAfterLockExpiry() {
            testUser.setFailedLoginAttempts(5);
            testUser.setLockedUntil(LocalDateTime.now().minusMinutes(1)); // đã hết hạn khóa

            LoginRequest request = LoginRequest.builder()
                    .username("admin01")
                    .password("Admin@123456")
                    .build();

            when(userRepository.findByUsername("admin01")).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("Admin@123456", testUser.getPasswordHash())).thenReturn(true);
            when(jwtService.generateAccessToken(testUser)).thenReturn("new-access-token");
            when(jwtService.generateRefreshTokenValue()).thenReturn("new-refresh-token");
            when(jwtService.getAccessTokenExpirationMs()).thenReturn(900000L);
            when(jwtService.getRefreshTokenExpirationMs()).thenReturn(604800000L);
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));
            when(loginHistoryRepository.save(any(LoginHistory.class))).thenAnswer(i -> i.getArgument(0));

            ApiResponse<LoginResponse> response = authService.login(request, httpServletRequest);

            assertThat(response.isSuccess()).isTrue();
            assertThat(testUser.getFailedLoginAttempts()).isEqualTo(0);
            assertThat(testUser.getLockedUntil()).isNull();
        }

        @Test
        @DisplayName("Đăng nhập khi đang bị khóa tạm thời (chưa hết hạn)")
        void loginWhileTemporarilyLocked() {
            testUser.setFailedLoginAttempts(5);
            testUser.setLockedUntil(LocalDateTime.now().plusMinutes(25)); // còn 25 phút

            LoginRequest request = LoginRequest.builder()
                    .username("admin01")
                    .password("Admin@123456")
                    .build();

            when(userRepository.findByUsername("admin01")).thenReturn(Optional.of(testUser));
            when(loginHistoryRepository.save(any(LoginHistory.class))).thenAnswer(i -> i.getArgument(0));

            assertThatThrownBy(() -> authService.login(request, httpServletRequest))
                    .isInstanceOf(AccountLockedException.class)
                    .hasMessageContaining("Vui lòng thử lại sau");
        }
    }

    // ==================== OTP TESTS ====================

    @Nested
    @DisplayName("UC-LOGIN-02: Đăng nhập OTP")
    class OtpTests {

        @Test
        @DisplayName("TC-OTP-01: Yêu cầu OTP thành công")
        void requestOtpSuccess() {
            OtpRequestDto request = OtpRequestDto.builder()
                    .identifier("admin01")
                    .build();

            when(userRepository.findByUsernameOrEmailOrPhone("admin01")).thenReturn(Optional.of(testUser));
            when(otpTokenRepository.countRecentOtpRequests(eq(testUser.getId()), any(LocalDateTime.class)))
                    .thenReturn(0L);
            when(otpTokenRepository.save(any(OtpToken.class))).thenAnswer(i -> i.getArgument(0));

            ApiResponse<Object> response = authService.requestOtp(request);

            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getMessage()).contains("OTP đã được gửi");
            verify(otpTokenRepository).invalidateAllOtpByUserId(testUser.getId());
            verify(otpTokenRepository).save(any(OtpToken.class));
        }

        @Test
        @DisplayName("TC-OTP-02: Xác minh OTP đúng → đăng nhập thành công")
        void verifyOtpSuccess() {
            OtpVerifyRequest request = OtpVerifyRequest.builder()
                    .identifier("admin01")
                    .otpCode("123456")
                    .build();

            OtpToken validOtp = OtpToken.builder()
                    .id(1L)
                    .user(testUser)
                    .otpCode("123456")
                    .expiresAt(LocalDateTime.now().plusMinutes(3))
                    .used(false)
                    .build();

            when(userRepository.findByUsernameOrEmailOrPhone("admin01")).thenReturn(Optional.of(testUser));
            when(otpTokenRepository.findValidOtp(eq(testUser.getId()), eq("123456"), any(LocalDateTime.class)))
                    .thenReturn(Optional.of(validOtp));
            when(otpTokenRepository.save(any(OtpToken.class))).thenAnswer(i -> i.getArgument(0));
            when(jwtService.generateAccessToken(testUser)).thenReturn("otp-access-token");
            when(jwtService.generateRefreshTokenValue()).thenReturn("otp-refresh-token");
            when(jwtService.getAccessTokenExpirationMs()).thenReturn(900000L);
            when(jwtService.getRefreshTokenExpirationMs()).thenReturn(604800000L);
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));
            when(loginHistoryRepository.save(any(LoginHistory.class))).thenAnswer(i -> i.getArgument(0));

            ApiResponse<LoginResponse> response = authService.verifyOtp(request, httpServletRequest);

            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getData().getAccessToken()).isEqualTo("otp-access-token");
            assertThat(validOtp.isUsed()).isTrue();
        }

        @Test
        @DisplayName("TC-OTP-04: Xác minh OTP sai")
        void verifyOtpInvalid() {
            OtpVerifyRequest request = OtpVerifyRequest.builder()
                    .identifier("admin01")
                    .otpCode("999999")
                    .build();

            when(userRepository.findByUsernameOrEmailOrPhone("admin01")).thenReturn(Optional.of(testUser));
            when(otpTokenRepository.findValidOtp(eq(testUser.getId()), eq("999999"), any(LocalDateTime.class)))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.verifyOtp(request, httpServletRequest))
                    .isInstanceOf(InvalidOtpException.class)
                    .hasMessageContaining("Mã OTP không đúng");
        }

        @Test
        @DisplayName("Rate limit: Yêu cầu OTP quá nhiều lần")
        void requestOtpRateLimited() {
            OtpRequestDto request = OtpRequestDto.builder()
                    .identifier("admin01")
                    .build();

            when(userRepository.findByUsernameOrEmailOrPhone("admin01")).thenReturn(Optional.of(testUser));
            when(otpTokenRepository.countRecentOtpRequests(eq(testUser.getId()), any(LocalDateTime.class)))
                    .thenReturn(3L); // đã đạt giới hạn

            assertThatThrownBy(() -> authService.requestOtp(request))
                    .isInstanceOf(InvalidOtpException.class)
                    .hasMessageContaining("quá nhiều lần");
        }
    }

    // ==================== LOGOUT TESTS ====================

    @Nested
    @DisplayName("UC-LOGOUT-01: Đăng xuất")
    class LogoutTests {

        @Test
        @DisplayName("TC-LOGOUT-01: Đăng xuất thành công")
        void logoutSuccess() {
            LoginHistory activeSession = LoginHistory.builder()
                    .id(1L)
                    .user(testUser)
                    .loginTime(LocalDateTime.now().minusHours(1))
                    .loginMethod(LoginMethod.PASSWORD)
                    .success(true)
                    .build();

            when(userRepository.findByUsername("admin01")).thenReturn(Optional.of(testUser));
            when(loginHistoryRepository.findLatestActiveSession(testUser.getId()))
                    .thenReturn(Optional.of(activeSession));
            when(loginHistoryRepository.save(any(LoginHistory.class))).thenAnswer(i -> i.getArgument(0));

            ApiResponse<Void> response = authService.logout("admin01");

            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getMessage()).isEqualTo("Đăng xuất thành công");
            verify(refreshTokenRepository).revokeAllByUserId(testUser.getId());
            assertThat(activeSession.getLogoutTime()).isNotNull();
        }
    }

    // ==================== REFRESH TOKEN TESTS ====================

    @Nested
    @DisplayName("Refresh Token")
    class RefreshTokenTests {

        @Test
        @DisplayName("TC-LOGIN-05: Refresh token thành công")
        void refreshTokenSuccess() {
            RefreshToken validRefreshToken = RefreshToken.builder()
                    .id(1L)
                    .user(testUser)
                    .token("valid-refresh-token")
                    .expiresAt(LocalDateTime.now().plusDays(5))
                    .revoked(false)
                    .build();

            RefreshTokenRequest request = RefreshTokenRequest.builder()
                    .refreshToken("valid-refresh-token")
                    .build();

            when(refreshTokenRepository.findByTokenAndRevokedFalse("valid-refresh-token"))
                    .thenReturn(Optional.of(validRefreshToken));
            when(jwtService.generateAccessToken(testUser)).thenReturn("new-access-token");
            when(jwtService.getAccessTokenExpirationMs()).thenReturn(900000L);

            ApiResponse<LoginResponse> response = authService.refreshToken(request);

            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getData().getAccessToken()).isEqualTo("new-access-token");
            assertThat(response.getData().getRefreshToken()).isEqualTo("valid-refresh-token");
        }

        @Test
        @DisplayName("TC-LOGOUT-03: Dùng refresh token đã bị revoke")
        void refreshTokenRevoked() {
            RefreshTokenRequest request = RefreshTokenRequest.builder()
                    .refreshToken("revoked-token")
                    .build();

            when(refreshTokenRepository.findByTokenAndRevokedFalse("revoked-token"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.refreshToken(request))
                    .isInstanceOf(TokenExpiredException.class)
                    .hasMessageContaining("Refresh Token không hợp lệ");
        }

        @Test
        @DisplayName("Refresh token đã hết hạn")
        void refreshTokenExpired() {
            RefreshToken expiredToken = RefreshToken.builder()
                    .id(1L)
                    .user(testUser)
                    .token("expired-token")
                    .expiresAt(LocalDateTime.now().minusDays(1))
                    .revoked(false)
                    .build();

            RefreshTokenRequest request = RefreshTokenRequest.builder()
                    .refreshToken("expired-token")
                    .build();

            when(refreshTokenRepository.findByTokenAndRevokedFalse("expired-token"))
                    .thenReturn(Optional.of(expiredToken));
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));

            assertThatThrownBy(() -> authService.refreshToken(request))
                    .isInstanceOf(TokenExpiredException.class)
                    .hasMessageContaining("đã hết hạn");
        }
    }

    // ==================== PROFILE TESTS ====================

    @Nested
    @DisplayName("Profile")
    class ProfileTests {

        @Test
        @DisplayName("Lấy profile thành công")
        void getProfileSuccess() {
            when(userRepository.findByUsername("admin01")).thenReturn(Optional.of(testUser));

            ApiResponse<UserProfileResponse> response = authService.getProfile("admin01");

            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getData().getUsername()).isEqualTo("admin01");
            assertThat(response.getData().getRole()).isEqualTo("ADMIN");
            assertThat(response.getData().getEmail()).isEqualTo("admin@restaurant.com");
        }
    }
}
