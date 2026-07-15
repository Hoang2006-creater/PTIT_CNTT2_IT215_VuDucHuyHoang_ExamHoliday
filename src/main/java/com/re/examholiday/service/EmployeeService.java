package com.re.examholiday.service;

import com.re.examholiday.dto.request.CreateEmployeeRequest;
import com.re.examholiday.dto.request.UpdateEmployeeRequest;
import com.re.examholiday.dto.request.UpdatePositionRequest;
import com.re.examholiday.dto.request.UpdateSalaryRequest;
import com.re.examholiday.dto.response.ApiResponse;
import com.re.examholiday.dto.response.EmployeeResponse;

import java.util.List;

public interface EmployeeService {

    ApiResponse<List<EmployeeResponse>> getAllEmployees();

    ApiResponse<EmployeeResponse> getEmployeeDetail(Long id);

    ApiResponse<EmployeeResponse> createEmployee(CreateEmployeeRequest request);

    ApiResponse<EmployeeResponse> updateEmployee(Long id, UpdateEmployeeRequest request);

    ApiResponse<Void> deleteEmployee(Long id);

    ApiResponse<EmployeeResponse> updatePosition(Long id, UpdatePositionRequest request);

    ApiResponse<EmployeeResponse> updateSalary(Long id, UpdateSalaryRequest request);
}
