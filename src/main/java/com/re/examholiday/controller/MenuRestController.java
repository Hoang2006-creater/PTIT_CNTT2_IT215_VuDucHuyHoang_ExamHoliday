package com.re.examholiday.controller;

import com.re.examholiday.dto.response.ApiResponse;
import com.re.examholiday.model.Category;
import com.re.examholiday.model.MenuItem;
import com.re.examholiday.repository.CategoryRepository;
import com.re.examholiday.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MenuRestController {

    private final MenuItemRepository menuItemRepository;
    private final CategoryRepository categoryRepository;

    @GetMapping("/api/menu-items")
    public ResponseEntity<ApiResponse<List<MenuItem>>> getAllMenuItems() {
        List<MenuItem> items = menuItemRepository.findAll();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách món ăn thành công", items));
    }

    @GetMapping("/api/categories")
    public ResponseEntity<ApiResponse<List<Category>>> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách danh mục thành công", categories));
    }
}
