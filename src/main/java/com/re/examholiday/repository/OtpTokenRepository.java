package com.re.examholiday.repository;

import com.re.examholiday.model.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {

    @Query("SELECT o FROM OtpToken o WHERE o.user.id = :userId AND o.otpCode = :otpCode " +
            "AND o.used = false AND o.expiresAt > :now")
    Optional<OtpToken> findValidOtp(@Param("userId") Long userId,
                                     @Param("otpCode") String otpCode,
                                     @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE OtpToken o SET o.used = true WHERE o.user.id = :userId AND o.used = false")
    void invalidateAllOtpByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(o) FROM OtpToken o WHERE o.user.id = :userId AND o.createdAt > :since")
    long countRecentOtpRequests(@Param("userId") Long userId, @Param("since") LocalDateTime since);
}
