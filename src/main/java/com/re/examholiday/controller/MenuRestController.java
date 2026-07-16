package com.re.examholiday.controller;

import com.re.examholiday.dto.response.ApiResponse;
import com.re.examholiday.model.Category;
import com.re.examholiday.model.MenuItem;
import com.re.examholiday.repository.CategoryRepository;
import com.re.examholiday.service.MenuItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class MenuRestController {

    private final MenuItemService menuItemService;
    private final CategoryRepository categoryRepository;

    @GetMapping("/api/menu-items")
    public ResponseEntity<ApiResponse<List<MenuItem>>> getAllMenuItems() {
        ApiResponse<List<MenuItem>> response = menuItemService.getAllMenuItems();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/menu-items/{id}")
    public ResponseEntity<ApiResponse<MenuItem>> getMenuItemDetail(@PathVariable Long id) {
        ApiResponse<MenuItem> response = menuItemService.getMenuItemDetail(id);
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/menu-items")
    public ResponseEntity<ApiResponse<MenuItem>> createMenuItem(
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("price") BigDecimal price,
            @RequestParam("categoryId") Integer categoryId,
            @RequestParam(value = "available", required = false) Boolean available,
            @RequestParam(value = "image", required = false) MultipartFile image) {

        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Tên món ăn không được để trống"));
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Giá tiền không hợp lệ"));
        }
        if (categoryId == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Danh mục không được để trống"));
        }

        ResponseEntity<ApiResponse<MenuItem>> imageValidationError = validateImage(image);
        if (imageValidationError != null) {
            return imageValidationError;
        }

        ApiResponse<MenuItem> response = menuItemService.createMenuItem(name, description, price, categoryId, available, image);
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PutMapping("/api/menu-items/{id}")
    public ResponseEntity<ApiResponse<MenuItem>> updateMenuItem(
            @PathVariable Long id,
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("price") BigDecimal price,
            @RequestParam("categoryId") Integer categoryId,
            @RequestParam(value = "available", required = false) Boolean available,
            @RequestParam(value = "image", required = false) MultipartFile image) {

        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Tên món ăn không được để trống"));
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Giá tiền không hợp lệ"));
        }
        if (categoryId == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Danh mục không được để trống"));
        }

        ResponseEntity<ApiResponse<MenuItem>> imageValidationError = validateImage(image);
        if (imageValidationError != null) {
            return imageValidationError;
        }

        ApiResponse<MenuItem> response = menuItemService.updateMenuItem(id, name, description, price, categoryId, available, image);
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/api/menu-items/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMenuItem(@PathVariable Long id) {
        ApiResponse<Void> response = menuItemService.deleteMenuItem(id);
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/categories")
    public ResponseEntity<ApiResponse<List<Category>>> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách danh mục thành công", categories));
    }

    private ResponseEntity<ApiResponse<MenuItem>> validateImage(MultipartFile image) {
        if (image != null && !image.isEmpty()) {
            String contentType = image.getContentType();
            if (contentType == null || (!contentType.equals("image/jpeg") &&
                                         !contentType.equals("image/jpg") &&
                                         !contentType.equals("image/png") &&
                                         !contentType.equals("image/webp"))) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Định dạng ảnh không hợp lệ. Chỉ chấp nhận JPG, JPEG, PNG, WEBP."));
            }
            if (image.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Kích thước ảnh không được vượt quá 5MB."));
            }
        }
        return null;
    }
}
