package com.re.examholiday.security;

import com.re.examholiday.model.Role;
import com.re.examholiday.model.User;
import com.re.examholiday.model.enumeration.RoleName;
import com.re.examholiday.model.enumeration.UserStatus;
import com.re.examholiday.service.JwtService;
import com.re.examholiday.service.impl.JwtServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;
    private User testUser;

    @BeforeEach
    void setUp() {
        jwtService = new JwtServiceImpl();

        // Set config via reflection (simulating @Value injection)
        ReflectionTestUtils.setField(jwtService, "jwtSecret",
                "RMS_TestSecretKey_2026_VuDucHuyHoang_PTIT_CNTT2_RestaurantMgmt_256bit!!");
        ReflectionTestUtils.setField(jwtService, "accessTokenExpirationMs", 900000L);
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpirationMs", 604800000L);

        Role role = Role.builder().id(1).name(RoleName.ADMIN).build();
        testUser = User.builder()
                .id(1L)
                .username("admin01")
                .passwordHash("hashed")
                .role(role)
                .status(UserStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("Tạo Access Token thành công")
    void generateAccessToken() {
        String token = jwtService.generateAccessToken(testUser);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // Header.Payload.Signature
    }

    @Test
    @DisplayName("Validate token hợp lệ")
    void validateValidToken() {
        String token = jwtService.generateAccessToken(testUser);

        assertThat(jwtService.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("Validate token không hợp lệ")
    void validateInvalidToken() {
        assertThat(jwtService.validateToken("invalid.token.value")).isFalse();
        assertThat(jwtService.validateToken("")).isFalse();
        assertThat(jwtService.validateToken(null)).isFalse();
    }

    @Test
    @DisplayName("Extract username từ token")
    void extractUsername() {
        String token = jwtService.generateAccessToken(testUser);

        String username = jwtService.extractUsername(token);

        assertThat(username).isEqualTo("admin01");
    }

    @Test
    @DisplayName("Extract role từ token")
    void extractRole() {
        String token = jwtService.generateAccessToken(testUser);

        String role = jwtService.extractRole(token);

        assertThat(role).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("Tạo Refresh Token Value (UUID)")
    void generateRefreshTokenValue() {
        String token1 = jwtService.generateRefreshTokenValue();
        String token2 = jwtService.generateRefreshTokenValue();

        assertThat(token1).isNotNull();
        assertThat(token1).isNotEmpty();
        assertThat(token1).isNotEqualTo(token2); // mỗi lần phải khác nhau
        assertThat(token1.length()).isEqualTo(64); // 2 UUIDs without dashes = 64 chars
    }

    @Test
    @DisplayName("Token chứa đúng claims")
    void tokenContainsCorrectClaims() {
        String token = jwtService.generateAccessToken(testUser);

        var claims = jwtService.extractAllClaims(token);

        assertThat(claims.getSubject()).isEqualTo("admin01");
        assertThat(claims.get("role", String.class)).isEqualTo("ADMIN");
        assertThat(claims.get("userId", Long.class)).isEqualTo(1L);
        assertThat(claims.getIssuedAt()).isNotNull();
        assertThat(claims.getExpiration()).isNotNull();
        assertThat(claims.getExpiration()).isAfter(claims.getIssuedAt());
    }

    @Test
    @DisplayName("Expiration time getters")
    void expirationTimeGetters() {
        assertThat(jwtService.getAccessTokenExpirationMs()).isEqualTo(900000L);
        assertThat(jwtService.getRefreshTokenExpirationMs()).isEqualTo(604800000L);
    }
}
