package com.re.examholiday.repository;

import com.re.examholiday.model.Order;
import com.re.examholiday.model.enumeration.OrderStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByTableId(Integer tableId);
    List<Order> findByStatus(OrderStatus status);
    List<Order> findByCustomerId(Long customerId);
    boolean existsByEmployeeId(Long employeeId);

    @Query("SELECT o.table, COUNT(o) as orderCount " +
           "FROM Order o " +
           "WHERE o.orderTime >= :startTime AND o.orderTime <= :endTime " +
           "GROUP BY o.table " +
           "ORDER BY orderCount DESC")
    List<Object[]> findMostUsedTables(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable);

    @Query("SELECT o.customer, SUM(o.totalAmount) as totalSpent, COUNT(o) as orderCount " +
           "FROM Order o " +
           "WHERE o.customer IS NOT NULL AND o.status = :status " +
           "AND o.orderTime >= :startTime AND o.orderTime <= :endTime " +
           "GROUP BY o.customer " +
           "ORDER BY totalSpent DESC")
    List<Object[]> findLoyalCustomers(
            @Param("status") OrderStatus status,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable);

    @Query("SELECT o.employee, COUNT(o) as orderCount " +
           "FROM Order o " +
           "WHERE o.orderTime >= :startTime AND o.orderTime <= :endTime " +
           "GROUP BY o.employee " +
           "ORDER BY orderCount DESC")
    List<Object[]> findTopEmployees(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable);
}
