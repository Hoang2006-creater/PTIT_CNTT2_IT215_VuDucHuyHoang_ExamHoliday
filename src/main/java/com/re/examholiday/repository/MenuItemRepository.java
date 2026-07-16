package com.re.examholiday.repository;

import com.re.examholiday.model.MenuItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByCategoryId(Integer categoryId);
    List<MenuItem> findByIsAvailable(Boolean isAvailable);
    Optional<MenuItem> findByName(String name);

    Page<MenuItem> findByIsAvailable(Boolean isAvailable, Pageable pageable);
    Page<MenuItem> findByNameContainingIgnoreCaseAndIsAvailable(String name, Boolean isAvailable, Pageable pageable);
    Page<MenuItem> findByCategoryIdAndIsAvailable(Integer categoryId, Boolean isAvailable, Pageable pageable);
}
