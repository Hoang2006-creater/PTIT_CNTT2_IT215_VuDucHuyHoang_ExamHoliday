package com.re.examholiday.controller;

import com.re.examholiday.dto.request.CreateUserRequest;
import com.re.examholiday.dto.request.ResetPasswordRequest;
import com.re.examholiday.dto.request.UpdateUserRequest;
import com.re.examholiday.dto.response.ApiResponse;
import com.re.examholiday.dto.response.UserResponse;
import com.re.examholiday.service.UserManageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/manage")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserManageController {

    private final UserManageService userManageService;

    /**
     * GET /api/users/manage/list - Xem danh sách tài khoản
     */
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        ApiResponse<List<UserResponse>> response = userManageService.getAllUsers();
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/users/manage/detail/{id} - Xem chi tiết tài khoản
     */
    @GetMapping("/detail/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserDetail(@PathVariable Long id) {
        ApiResponse<UserResponse> response = userManageService.getUserDetail(id);
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/users/manage/create - Tạo tài khoản nhân viên
     */
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request) {
        ApiResponse<UserResponse> response = userManageService.createUser(request);
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/users/manage/update/{id} - Cập nhật thông tin tài khoản
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        ApiResponse<UserResponse> response = userManageService.updateUser(id, request);
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/users/manage/{id}/lock - Khóa tài khoản
     */
    @PutMapping("/{id}/lock")
    public ResponseEntity<ApiResponse<Void>> lockUser(@PathVariable Long id) {
        ApiResponse<Void> response = userManageService.lockUser(id);
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/users/manage/{id}/unlock - Mở khóa tài khoản
     */
    @PutMapping("/{id}/unlock")
    public ResponseEntity<ApiResponse<Void>> unlockUser(@PathVariable Long id) {
        ApiResponse<Void> response = userManageService.unlockUser(id);
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/users/manage/{id}/reset-password - Đặt lại mật khẩu tài khoản
     */
    @PutMapping("/{id}/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @PathVariable Long id,
            @Valid @RequestBody ResetPasswordRequest request) {
        ApiResponse<Void> response = userManageService.resetPassword(id, request);
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }
}
