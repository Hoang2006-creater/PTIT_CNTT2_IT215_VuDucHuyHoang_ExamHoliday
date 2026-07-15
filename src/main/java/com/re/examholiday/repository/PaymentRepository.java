package com.re.examholiday.repository;

import com.re.examholiday.model.Payment;
import com.re.examholiday.model.enumeration.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(Long orderId);
    boolean existsByCashierId(Long cashierId);

    @Query("SELECT SUM(p.amount) FROM Payment p " +
           "WHERE p.status = :status AND p.paymentTime >= :startTime AND p.paymentTime <= :endTime")
    BigDecimal sumRevenueByTimeRange(
            @Param("status") PaymentStatus status,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
}
