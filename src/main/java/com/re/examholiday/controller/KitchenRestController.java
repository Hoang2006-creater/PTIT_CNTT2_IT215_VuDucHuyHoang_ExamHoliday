package com.re.examholiday.controller;

import com.re.examholiday.dto.response.ApiResponse;
import com.re.examholiday.model.OrderDetail;
import com.re.examholiday.model.enumeration.OrderItemStatus;
import com.re.examholiday.repository.OrderDetailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/kitchen")
@RequiredArgsConstructor
public class KitchenRestController {

    private final OrderDetailRepository orderDetailRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderDetail>>> getKitchenDishes() {
        List<OrderDetail> ordered = orderDetailRepository.findByStatus(OrderItemStatus.ORDERED);
        List<OrderDetail> cooking = orderDetailRepository.findByStatus(OrderItemStatus.COOKING);
        
        List<OrderDetail> kitchenList = new ArrayList<>();
        if (ordered != null) kitchenList.addAll(ordered);
        if (cooking != null) kitchenList.addAll(cooking);
        
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách chế biến nhà bếp thành công", kitchenList));
    }
}
