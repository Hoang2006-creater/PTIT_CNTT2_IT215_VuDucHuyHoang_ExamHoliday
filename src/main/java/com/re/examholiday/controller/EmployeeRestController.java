package com.re.examholiday.controller;

import com.re.examholiday.dto.request.CreateEmployeeRequest;
import com.re.examholiday.dto.request.UpdateEmployeeRequest;
import com.re.examholiday.dto.request.UpdatePositionRequest;
import com.re.examholiday.dto.request.UpdateSalaryRequest;
import com.re.examholiday.dto.response.ApiResponse;
import com.re.examholiday.dto.response.EmployeeResponse;
import com.re.examholiday.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class EmployeeRestController {

    private final EmployeeService employeeService;

    /**
     * GET /api/employees - Danh sách nhân viên
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> getAllEmployees() {
        ApiResponse<List<EmployeeResponse>> response = employeeService.getAllEmployees();
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/employees/{id} - Chi tiết nhân viên
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeResponse>> getEmployeeDetail(@PathVariable Long id) {
        ApiResponse<EmployeeResponse> response = employeeService.getEmployeeDetail(id);
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/employees - Thêm nhân viên mới
     */
    @PostMapping
    public ResponseEntity<ApiResponse<EmployeeResponse>> createEmployee(
            @Valid @RequestBody CreateEmployeeRequest request) {
        ApiResponse<EmployeeResponse> response = employeeService.createEmployee(request);
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/employees/{id} - Cập nhật nhân viên
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEmployeeRequest request) {
        ApiResponse<EmployeeResponse> response = employeeService.updateEmployee(id, request);
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/employees/{id} - Xóa nhân viên
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEmployee(@PathVariable Long id) {
        ApiResponse<Void> response = employeeService.deleteEmployee(id);
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/employees/{id}/position - Cập nhật vị trí làm việc
     */
    @PutMapping("/{id}/position")
    public ResponseEntity<ApiResponse<EmployeeResponse>> updatePosition(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePositionRequest request) {
        ApiResponse<EmployeeResponse> response = employeeService.updatePosition(id, request);
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/employees/{id}/salary - Quản lý lương (Cập nhật lương)
     */
    @PutMapping("/{id}/salary")
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateSalary(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSalaryRequest request) {
        ApiResponse<EmployeeResponse> response = employeeService.updateSalary(id, request);
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }
}
