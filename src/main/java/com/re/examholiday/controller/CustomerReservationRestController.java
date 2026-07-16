package com.re.examholiday.controller;

import com.re.examholiday.dto.request.CreateReservationRequest;
import com.re.examholiday.dto.response.ApiResponse;
import com.re.examholiday.model.Reservation;
import com.re.examholiday.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer/reservations")
@RequiredArgsConstructor
public class CustomerReservationRestController {

    private final ReservationService reservationService;

    /**
     * POST /api/customer/reservations - Đặt bàn ăn mới
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Reservation>> createReservation(
            Authentication authentication,
            @Valid @RequestBody CreateReservationRequest request) {
        ApiResponse<Reservation> response = reservationService.createReservation(authentication.getName(), request);
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/customer/reservations/active - Danh sách đặt bàn hoạt động (PENDING, CONFIRMED)
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<Reservation>>> getActiveReservations(Authentication authentication) {
        ApiResponse<List<Reservation>> response = reservationService.getActiveReservations(authentication.getName());
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/customer/reservations/history - Lịch sử đặt bàn ăn của khách hàng
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<Reservation>>> getReservationHistory(Authentication authentication) {
        ApiResponse<List<Reservation>> response = reservationService.getReservationHistory(authentication.getName());
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/customer/reservations/{id}/cancel - Hủy lịch đặt bàn ăn
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelReservation(
            Authentication authentication,
            @PathVariable Long id) {
        ApiResponse<Void> response = reservationService.cancelReservation(authentication.getName(), id);
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }
}
