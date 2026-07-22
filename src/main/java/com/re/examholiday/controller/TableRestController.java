package com.re.examholiday.controller;

import com.re.examholiday.dto.response.ApiResponse;
import com.re.examholiday.model.Order;
import com.re.examholiday.model.Reservation;
import com.re.examholiday.model.RestaurantTable;
import com.re.examholiday.model.enumeration.OrderStatus;
import com.re.examholiday.model.enumeration.ReservationStatus;
import com.re.examholiday.model.enumeration.TableStatus;
import com.re.examholiday.repository.OrderRepository;
import com.re.examholiday.repository.ReservationRepository;
import com.re.examholiday.repository.RestaurantTableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tables")
@RequiredArgsConstructor
public class TableRestController {

    private final RestaurantTableRepository restaurantTableRepository;
    private final OrderRepository orderRepository;
    private final ReservationRepository reservationRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RestaurantTable>>> getAllTables() {
        List<RestaurantTable> tables = restaurantTableRepository.findAll();
        List<Order> openOrders = orderRepository.findByStatus(OrderStatus.OPEN);
        List<Reservation> activeReservations = reservationRepository.findByStatusIn(
                List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED)
        );

        Set<Integer> occupiedTableIds = openOrders.stream()
                .filter(o -> o.getTable() != null)
                .map(o -> o.getTable().getId())
                .collect(Collectors.toSet());

        Set<Integer> reservedTableIds = activeReservations.stream()
                .filter(r -> r.getTable() != null)
                .map(r -> r.getTable().getId())
                .collect(Collectors.toSet());

        for (RestaurantTable t : tables) {
            TableStatus newStatus = TableStatus.EMPTY;
            if (occupiedTableIds.contains(t.getId())) {
                newStatus = TableStatus.OCCUPIED;
            } else if (reservedTableIds.contains(t.getId())) {
                newStatus = TableStatus.RESERVED;
            }
            if (t.getStatus() != newStatus) {
                t.setStatus(newStatus);
                restaurantTableRepository.save(t);
            }
        }

        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách bàn ăn thành công", tables));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<RestaurantTable>> updateTableStatus(
            @PathVariable Integer id,
            @RequestParam TableStatus status) {
        RestaurantTable table = restaurantTableRepository.findById(id).orElse(null);
        if (table == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Không tìm thấy bàn ăn"));
        }
        table.setStatus(status);
        RestaurantTable saved = restaurantTableRepository.save(table);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái bàn ăn thành công", saved));
    }
}
