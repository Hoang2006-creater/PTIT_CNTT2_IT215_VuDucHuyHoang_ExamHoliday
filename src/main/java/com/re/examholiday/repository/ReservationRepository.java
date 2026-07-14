package com.re.examholiday.repository;

import com.re.examholiday.model.Reservation;
import com.re.examholiday.model.enumeration.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByReservationTimeBetween(LocalDateTime start, LocalDateTime end);
    List<Reservation> findByStatus(ReservationStatus status);
    List<Reservation> findByCustomerPhone(String phone);
}
