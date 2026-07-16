package com.re.examholiday.repository;

import com.re.examholiday.model.Reservation;
import com.re.examholiday.model.enumeration.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByReservationTimeBetween(LocalDateTime start, LocalDateTime end);
    List<Reservation> findByStatus(ReservationStatus status);
    List<Reservation> findByCustomerPhone(String phone);
    boolean existsByCustomerId(Long customerId);

    List<Reservation> findByCustomerId(Long customerId);
    List<Reservation> findByCustomerIdAndStatusIn(Long customerId, List<ReservationStatus> statuses);

    @Query("SELECT r FROM Reservation r WHERE r.table.id = :tableId AND r.status IN :statuses AND r.reservationTime > :start AND r.reservationTime < :end")
    List<Reservation> findOverlappingReservations(
            @Param("tableId") Integer tableId,
            @Param("statuses") List<ReservationStatus> statuses,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
