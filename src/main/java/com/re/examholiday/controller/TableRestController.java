package com.re.examholiday.controller;

import com.re.examholiday.dto.response.ApiResponse;
import com.re.examholiday.model.RestaurantTable;
import com.re.examholiday.repository.RestaurantTableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tables")
@RequiredArgsConstructor
public class TableRestController {

    private final RestaurantTableRepository restaurantTableRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RestaurantTable>>> getAllTables() {
        List<RestaurantTable> tables = restaurantTableRepository.findAll();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách bàn ăn thành công", tables));
    }
}
