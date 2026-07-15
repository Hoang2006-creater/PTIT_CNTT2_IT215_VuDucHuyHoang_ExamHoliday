package com.re.examholiday.repository;

import com.re.examholiday.model.OrderDetail;
import com.re.examholiday.model.enumeration.OrderItemStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
    List<OrderDetail> findByOrderId(Long orderId);
    List<OrderDetail> findByStatus(OrderItemStatus status);

    @Query("SELECT od.menuItem, SUM(od.quantity) as totalSold " +
           "FROM OrderDetail od " +
           "WHERE od.order.orderTime >= :startTime AND od.order.orderTime <= :endTime " +
           "GROUP BY od.menuItem " +
           "ORDER BY totalSold DESC")
    List<Object[]> findBestSellingMenuItems(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable);
}
