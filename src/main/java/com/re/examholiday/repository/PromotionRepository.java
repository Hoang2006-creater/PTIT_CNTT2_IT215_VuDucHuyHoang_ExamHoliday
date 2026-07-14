package com.re.examholiday.repository;

import com.re.examholiday.model.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Integer> {
    Optional<Promotion> findByCode(String code);
    List<Promotion> findByIsActiveTrueAndStartDateBeforeAndEndDateAfter(LocalDate startLimit, LocalDate endLimit);
}
