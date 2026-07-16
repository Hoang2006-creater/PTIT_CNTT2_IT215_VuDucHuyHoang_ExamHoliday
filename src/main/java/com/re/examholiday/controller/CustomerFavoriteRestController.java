package com.re.examholiday.controller;

import com.re.examholiday.dto.request.FavoriteRequest;
import com.re.examholiday.dto.response.ApiResponse;
import com.re.examholiday.dto.response.CustomerFavoriteResponse;
import com.re.examholiday.model.Favorite;
import com.re.examholiday.service.FavoriteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer/favorites")
@RequiredArgsConstructor
public class CustomerFavoriteRestController {

    private final FavoriteService favoriteService;

    /**
     * POST /api/customer/favorites/add - Thêm món ăn vào danh sách yêu thích
     */
    @PostMapping("/add")
    public ResponseEntity<ApiResponse<Favorite>> addFavorite(
            Authentication authentication,
            @Valid @RequestBody FavoriteRequest request) {
        ApiResponse<Favorite> response = favoriteService.addFavorite(authentication.getName(), request);
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/customer/favorites/remove/{menuItemId} - Xóa món ăn khỏi danh sách yêu thích
     */
    @DeleteMapping("/remove/{menuItemId}")
    public ResponseEntity<ApiResponse<Void>> removeFavorite(
            Authentication authentication,
            @PathVariable Long menuItemId) {
        ApiResponse<Void> response = favoriteService.removeFavorite(authentication.getName(), menuItemId);
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/customer/favorites - Lấy danh sách món ăn yêu thích của bản thân
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomerFavoriteResponse>>> getFavorites(Authentication authentication) {
        ApiResponse<List<CustomerFavoriteResponse>> response = favoriteService.getFavorites(authentication.getName());
        return ResponseEntity.ok(response);
    }
}
