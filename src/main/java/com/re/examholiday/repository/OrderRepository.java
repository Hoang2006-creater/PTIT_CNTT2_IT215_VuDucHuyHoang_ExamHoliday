package com.re.examholiday.repository;

import com.re.examholiday.model.Order;
import com.re.examholiday.model.enumeration.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByTableId(Integer tableId);
    List<Order> findByStatus(OrderStatus status);
    List<Order> findByCustomerId(Long customerId);
}
