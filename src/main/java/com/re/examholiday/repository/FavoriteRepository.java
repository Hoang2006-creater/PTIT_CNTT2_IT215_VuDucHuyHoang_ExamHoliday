package com.re.examholiday.repository;

import com.re.examholiday.model.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    List<Favorite> findByCustomerId(Long customerId);
    Optional<Favorite> findByCustomerIdAndMenuItemId(Long customerId, Long menuItemId);
    boolean existsByCustomerIdAndMenuItemId(Long customerId, Long menuItemId);
}
