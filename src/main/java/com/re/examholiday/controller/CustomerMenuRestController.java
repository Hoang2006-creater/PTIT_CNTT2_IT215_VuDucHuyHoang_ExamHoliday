package com.re.examholiday.controller;

import com.re.examholiday.dto.response.ApiResponse;
import com.re.examholiday.dto.response.CustomerMenuItemResponse;
import com.re.examholiday.service.MenuItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
public class CustomerMenuRestController {

    private final MenuItemService menuItemService;

    /**
     * GET /api/customer/menu - Danh sách món ăn có phân trang & sắp xếp
     */
    @GetMapping("/menu")
    public ResponseEntity<ApiResponse<Page<CustomerMenuItemResponse>>> getCustomerMenuItems(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        ApiResponse<Page<CustomerMenuItemResponse>> response = menuItemService.getCustomerMenuItems(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/customer/menu/{id} - Chi tiết món ăn
     */
    @GetMapping("/menu/{id}")
    public ResponseEntity<ApiResponse<CustomerMenuItemResponse>> getCustomerMenuItemDetail(@PathVariable Long id) {
        ApiResponse<CustomerMenuItemResponse> response = menuItemService.getCustomerMenuItemDetail(id);
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/customer/menu/search - Tìm kiếm món ăn có phân trang & sắp xếp
     */
    @GetMapping("/menu/search")
    public ResponseEntity<ApiResponse<Page<CustomerMenuItemResponse>>> searchCustomerMenuItems(
            @RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        ApiResponse<Page<CustomerMenuItemResponse>> response = menuItemService.searchCustomerMenuItems(keyword, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/customer/categories/{id}/menu - Lọc món ăn theo danh mục có phân trang & sắp xếp
     */
    @GetMapping("/categories/{id}/menu")
    public ResponseEntity<ApiResponse<Page<CustomerMenuItemResponse>>> getCustomerMenuItemsByCategory(
            @PathVariable("id") Integer categoryId,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        ApiResponse<Page<CustomerMenuItemResponse>> response = menuItemService.getCustomerMenuItemsByCategory(categoryId, pageable);
        return ResponseEntity.ok(response);
    }
}
