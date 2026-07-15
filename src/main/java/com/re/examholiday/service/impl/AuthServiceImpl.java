package com.re.examholiday.service.impl;

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
import com.re.examholiday.model.LoginHistory;
import com.re.examholiday.model.OtpToken;
import com.re.examholiday.model.RefreshToken;
import com.re.examholiday.model.User;
import com.re.examholiday.model.enumeration.LoginMethod;
import com.re.examholiday.model.enumeration.UserStatus;
import com.re.examholiday.repository.LoginHistoryRepository;
import com.re.examholiday.repository.OtpTokenRepository;
import com.re.examholiday.repository.RefreshTokenRepository;
import com.re.examholiday.repository.UserRepository;
import com.re.examholiday.service.AuthService;
import com.re.examholiday.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final OtpTokenRepository otpTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final LoginHistoryRepository loginHistoryRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Value("${security.max-failed-attempts:5}")
    private int maxFailedAttempts;

    @Value("${security.lock-duration-minutes:30}")
    private int lockDurationMinutes;

    @Value("${otp.expiration-minutes:5}")
    private int otpExpirationMinutes;

    @Value("${otp.max-requests-per-window:3}")
    private int otpMaxRequestsPerWindow;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    // ==================== LOGIN BY USERNAME/PASSWORD ====================

    @Override
    @Transactional
    public ApiResponse<LoginResponse> login(LoginRequest request, HttpServletRequest httpRequest) {
        String ipAddress = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        // Bước 1: Tìm user theo username
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    log.warn("Đăng nhập thất bại: username '{}' không tồn tại. IP: {}",
                            request.getUsername(), ipAddress);
                    return new AuthenticationException(
                            "Tên đăng nhập hoặc mật khẩu không đúng");
                });

        // Bước 2: Kiểm tra tài khoản bị BLOCKED
        if (user.getStatus() == UserStatus.BLOCKED) {
            saveLoginHistory(user, ipAddress, userAgent, LoginMethod.PASSWORD, false);
            throw new AccountLockedException(
                    "Tài khoản đã bị khóa. Vui lòng liên hệ quản trị viên.");
        }

        // Bước 3: Kiểm tra khóa tạm thời
        if (user.getLockedUntil() != null && LocalDateTime.now().isBefore(user.getLockedUntil())) {
            long minutesRemaining = ChronoUnit.MINUTES.between(LocalDateTime.now(), user.getLockedUntil()) + 1;
            saveLoginHistory(user, ipAddress, userAgent, LoginMethod.PASSWORD, false);
            throw new AccountLockedException(
                    "Tài khoản bị khóa tạm thời do đăng nhập sai quá nhiều lần. " +
                            "Vui lòng thử lại sau " + minutesRemaining + " phút.");
        }

        // Bước 3b: Reset lock nếu đã qua thời gian
        if (user.getLockedUntil() != null && LocalDateTime.now().isAfter(user.getLockedUntil())) {
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
        }

        // Bước 4: Kiểm tra mật khẩu
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            return handleFailedLogin(user, ipAddress, userAgent);
        }

        // Bước 5: Đăng nhập thành công
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);

        LoginResponse loginResponse = generateTokens(user);
        saveLoginHistory(user, ipAddress, userAgent, LoginMethod.PASSWORD, true);

        log.info("Đăng nhập thành công: username='{}', role='{}', IP='{}'",
                user.getUsername(), user.getRole().getName(), ipAddress);

        return ApiResponse.success("Đăng nhập thành công", loginResponse);
    }

    // ==================== OTP REQUEST ====================

    @Override
    @Transactional
    public ApiResponse<Object> requestOtp(OtpRequestDto request) {
        // Bước 1: Tìm user theo identifier (username/email/phone)
        User user = userRepository.findByUsernameOrEmailOrPhone(request.getIdentifier())
                .orElseThrow(() -> new AuthenticationException(
                        "Không tìm thấy tài khoản với thông tin đã cung cấp."));

        // Bước 2: Kiểm tra trạng thái
        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new AccountLockedException(
                    "Tài khoản đã bị khóa. Vui lòng liên hệ quản trị viên.");
        }

        // Bước 3: Rate limit OTP (tối đa 3 lần / 5 phút)
        long recentOtpCount = otpTokenRepository.countRecentOtpRequests(
                user.getId(), LocalDateTime.now().minusMinutes(5));
        if (recentOtpCount >= otpMaxRequestsPerWindow) {
            throw new InvalidOtpException(
                    "Bạn đã yêu cầu OTP quá nhiều lần. Vui lòng thử lại sau 5 phút.");
        }

        // Bước 4: Vô hiệu hóa OTP cũ
        otpTokenRepository.invalidateAllOtpByUserId(user.getId());

        // Bước 5: Tạo OTP 6 chữ số
        String otpCode = generateOtpCode();

        // Bước 6: Lưu OTP
        OtpToken otpToken = OtpToken.builder()
                .user(user)
                .otpCode(otpCode)
                .expiresAt(LocalDateTime.now().plusMinutes(otpExpirationMinutes))
                .used(false)
                .build();
        otpTokenRepository.save(otpToken);

        // Bước 7: Gửi OTP (log thay cho SMS/Email trong dev)
        String maskedTarget = maskTarget(user);
        log.info("OTP '{}' đã được tạo cho user '{}'. Gửi đến: {}",
                otpCode, user.getUsername(), maskedTarget);

        Map<String, Object> responseData = Map.of(
                "expiresInSeconds", otpExpirationMinutes * 60,
                "maskedTarget", maskedTarget
        );

        return ApiResponse.success(
                "Mã OTP đã được gửi đến " + maskedTarget + ". Mã có hiệu lực trong "
                        + otpExpirationMinutes + " phút.",
                responseData);
    }

    // ==================== OTP VERIFY ====================

    @Override
    @Transactional
    public ApiResponse<LoginResponse> verifyOtp(OtpVerifyRequest request, HttpServletRequest httpRequest) {
        String ipAddress = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        // Bước 1: Tìm user
        User user = userRepository.findByUsernameOrEmailOrPhone(request.getIdentifier())
                .orElseThrow(() -> new AuthenticationException(
                        "Không tìm thấy tài khoản với thông tin đã cung cấp."));

        // Bước 2: Kiểm tra trạng thái
        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new AccountLockedException(
                    "Tài khoản đã bị khóa. Vui lòng liên hệ quản trị viên.");
        }

        // Bước 3: Tìm OTP hợp lệ
        OtpToken otpToken = otpTokenRepository.findValidOtp(
                        user.getId(), request.getOtpCode(), LocalDateTime.now())
                .orElseThrow(() -> new InvalidOtpException(
                        "Mã OTP không đúng hoặc đã hết hạn. Vui lòng yêu cầu mã mới."));

        // Bước 4: Đánh dấu OTP đã sử dụng
        otpToken.setUsed(true);
        otpTokenRepository.save(otpToken);

        // Bước 5: Tạo token và ghi log
        LoginResponse loginResponse = generateTokens(user);
        saveLoginHistory(user, ipAddress, userAgent, LoginMethod.OTP, true);

        log.info("Đăng nhập OTP thành công: username='{}', IP='{}'",
                user.getUsername(), ipAddress);

        return ApiResponse.success("Xác thực OTP thành công", loginResponse);
    }

    // ==================== LOGOUT ====================

    @Override
    @Transactional
    public ApiResponse<Void> logout(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthenticationException("Người dùng không tồn tại."));

        // Revoke tất cả refresh token
        refreshTokenRepository.revokeAllByUserId(user.getId());

        // Cập nhật logoutTime trong LoginHistory
        loginHistoryRepository.findLatestActiveSession(user.getId())
                .ifPresent(history -> {
                    history.setLogoutTime(LocalDateTime.now());
                    loginHistoryRepository.save(history);
                });

        log.info("Đăng xuất thành công: username='{}'", username);

        return ApiResponse.success("Đăng xuất thành công");
    }

    // ==================== REFRESH TOKEN ====================

    @Override
    @Transactional
    public ApiResponse<LoginResponse> refreshToken(RefreshTokenRequest request) {
        // Tìm refresh token hợp lệ (chưa revoke)
        RefreshToken refreshToken = refreshTokenRepository
                .findByTokenAndRevokedFalse(request.getRefreshToken())
                .orElseThrow(() -> new TokenExpiredException(
                        "Refresh Token không hợp lệ hoặc đã hết hạn. Vui lòng đăng nhập lại."));

        // Kiểm tra hết hạn
        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
            throw new TokenExpiredException(
                    "Refresh Token đã hết hạn. Vui lòng đăng nhập lại.");
        }

        // Tạo access token mới
        User user = refreshToken.getUser();
        String accessToken = jwtService.generateAccessToken(user);

        LoginResponse loginResponse = LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpirationMs() / 1000)
                .role(user.getRole().getName().name())
                .username(user.getUsername())
                .build();

        log.info("Token refreshed cho username='{}'", user.getUsername());

        return ApiResponse.success("Token đã được làm mới", loginResponse);
    }

    // ==================== GET PROFILE ====================

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<UserProfileResponse> getProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthenticationException("Người dùng không tồn tại."));

        UserProfileResponse profile = UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().getName().name())
                .status(user.getStatus().name())
                .createdAt(user.getCreatedAt())
                .build();

        return ApiResponse.success("Thành công", profile);
    }

    // ==================== PRIVATE HELPER METHODS ====================

    private ApiResponse<LoginResponse> handleFailedLogin(User user, String ipAddress, String userAgent) {
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);

        if (attempts >= maxFailedAttempts) {
            user.setLockedUntil(LocalDateTime.now().plusMinutes(lockDurationMinutes));
            userRepository.save(user);
            saveLoginHistory(user, ipAddress, userAgent, LoginMethod.PASSWORD, false);
            log.warn("Tài khoản '{}' bị khóa {} phút do đăng nhập sai {} lần",
                    user.getUsername(), lockDurationMinutes, maxFailedAttempts);
            throw new AccountLockedException(
                    "Tài khoản bị khóa " + lockDurationMinutes +
                            " phút do đăng nhập sai quá nhiều lần.");
        }

        userRepository.save(user);
        saveLoginHistory(user, ipAddress, userAgent, LoginMethod.PASSWORD, false);

        int remaining = maxFailedAttempts - attempts;
        throw new AuthenticationException(
                "Tên đăng nhập hoặc mật khẩu không đúng. Còn " + remaining + " lần thử.");
    }

    @Transactional
    protected LoginResponse generateTokens(User user) {
        // Revoke refresh token cũ
        refreshTokenRepository.revokeAllByUserId(user.getId());

        // Tạo access token
        String accessToken = jwtService.generateAccessToken(user);

        // Tạo refresh token
        String refreshTokenValue = jwtService.generateRefreshTokenValue();
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(refreshTokenValue)
                .expiresAt(LocalDateTime.now().plusSeconds(
                        jwtService.getRefreshTokenExpirationMs() / 1000))
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpirationMs() / 1000)
                .role(user.getRole().getName().name())
                .username(user.getUsername())
                .build();
    }

    private void saveLoginHistory(User user, String ipAddress, String userAgent,
                                   LoginMethod loginMethod, boolean success) {
        LoginHistory history = LoginHistory.builder()
                .user(user)
                .loginTime(LocalDateTime.now())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .loginMethod(loginMethod)
                .success(success)
                .build();
        loginHistoryRepository.save(history);
    }

    private String generateOtpCode() {
        int otp = 100000 + SECURE_RANDOM.nextInt(900000);
        return String.valueOf(otp);
    }

    private String maskTarget(User user) {
        if (user.getPhone() != null && !user.getPhone().isEmpty()) {
            String phone = user.getPhone();
            return "****" + phone.substring(Math.max(0, phone.length() - 4));
        }
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            String email = user.getEmail();
            int atIndex = email.indexOf('@');
            if (atIndex > 2) {
                return email.substring(0, 2) + "****" + email.substring(atIndex);
            }
            return "****" + email.substring(atIndex);
        }
        return "****";
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
}
