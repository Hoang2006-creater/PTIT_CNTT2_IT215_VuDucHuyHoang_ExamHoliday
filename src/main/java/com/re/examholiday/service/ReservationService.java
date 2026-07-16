package com.re.examholiday.service;

import com.re.examholiday.dto.request.CreateReservationRequest;
import com.re.examholiday.dto.response.ApiResponse;
import com.re.examholiday.model.Reservation;

import java.util.List;

public interface ReservationService {
    ApiResponse<Reservation> createReservation(String username, CreateReservationRequest request);
    ApiResponse<List<Reservation>> getActiveReservations(String username);
    ApiResponse<List<Reservation>> getReservationHistory(String username);
    ApiResponse<Void> cancelReservation(String username, Long reservationId);
}
