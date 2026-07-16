package com.re.examholiday.service;

import com.re.examholiday.dto.response.ApiResponse;
import com.re.examholiday.model.MenuItem;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

public interface MenuItemService {
    ApiResponse<List<MenuItem>> getAllMenuItems();
    ApiResponse<MenuItem> getMenuItemDetail(Long id);
    ApiResponse<MenuItem> createMenuItem(String name, String description, BigDecimal price, Integer categoryId, Boolean available, MultipartFile image);
    ApiResponse<MenuItem> updateMenuItem(Long id, String name, String description, BigDecimal price, Integer categoryId, Boolean available, MultipartFile image);
    ApiResponse<Void> deleteMenuItem(Long id);
}
