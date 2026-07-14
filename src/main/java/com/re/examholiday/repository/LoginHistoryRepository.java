package com.re.examholiday.repository;

import com.re.examholiday.model.LoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {

    @Query("SELECT lh FROM LoginHistory lh WHERE lh.user.id = :userId AND lh.success = true " +
            "AND lh.logoutTime IS NULL ORDER BY lh.loginTime DESC LIMIT 1")
    Optional<LoginHistory> findLatestActiveSession(@Param("userId") Long userId);
}
