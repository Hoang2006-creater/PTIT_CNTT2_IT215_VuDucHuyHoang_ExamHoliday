package com.re.examholiday.repository;

import com.re.examholiday.model.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByCategoryId(Integer categoryId);
    List<MenuItem> findByIsAvailable(Boolean isAvailable);
    Optional<MenuItem> findByName(String name);
}
