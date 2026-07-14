package com.re.examholiday.service;

import com.re.examholiday.model.User;
import io.jsonwebtoken.Claims;

public interface JwtService {

    String generateAccessToken(User user);

    String generateRefreshTokenValue();

    boolean validateToken(String token);

    String extractUsername(String token);

    String extractRole(String token);

    Claims extractAllClaims(String token);

    long getAccessTokenExpirationMs();

    long getRefreshTokenExpirationMs();
}
