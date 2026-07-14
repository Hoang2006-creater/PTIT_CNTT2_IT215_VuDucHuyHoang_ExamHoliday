package com.re.examholiday.repository;

import com.re.examholiday.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByCustomerId(Long customerId);
    Optional<Review> findByOrderId(Long orderId);
    List<Review> findByRating(Integer rating);
}
