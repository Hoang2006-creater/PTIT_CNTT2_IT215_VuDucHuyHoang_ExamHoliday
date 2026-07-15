package com.re.examholiday.service.impl;

import com.re.examholiday.dto.request.CreateEmployeeRequest;
import com.re.examholiday.dto.request.UpdateEmployeeRequest;
import com.re.examholiday.dto.request.UpdatePositionRequest;
import com.re.examholiday.dto.request.UpdateSalaryRequest;
import com.re.examholiday.dto.response.ApiResponse;
import com.re.examholiday.dto.response.EmployeeResponse;
import com.re.examholiday.model.Employee;
import com.re.examholiday.model.User;
import com.re.examholiday.model.enumeration.EmployeeStatus;
import com.re.examholiday.repository.EmployeeRepository;
import com.re.examholiday.repository.OrderRepository;
import com.re.examholiday.repository.PaymentRepository;
import com.re.examholiday.repository.UserRepository;
import com.re.examholiday.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    @Override
    public ApiResponse<List<EmployeeResponse>> getAllEmployees() {
        List<Employee> employees = employeeRepository.findAll();
        List<EmployeeResponse> responseList = employees.stream()
                .map(this::mapToEmployeeResponse)
                .collect(Collectors.toList());
        return ApiResponse.success("Lấy danh sách nhân viên thành công", responseList);
    }

    @Override
    public ApiResponse<EmployeeResponse> getEmployeeDetail(Long id) {
        Employee employee = employeeRepository.findById(id).orElse(null);
        if (employee == null) {
            return ApiResponse.error("Không tìm thấy nhân viên");
        }
        return ApiResponse.success("Lấy chi tiết nhân viên thành công", mapToEmployeeResponse(employee));
    }

    @Override
    @Transactional
    public ApiResponse<EmployeeResponse> createEmployee(CreateEmployeeRequest request) {
        User user = null;
        if (request.getUserId() != null) {
            user = userRepository.findById(request.getUserId()).orElse(null);
            if (user == null) {
                return ApiResponse.error("Không tìm thấy tài khoản để liên kết");
            }
            if (employeeRepository.findByUserId(request.getUserId()).isPresent()) {
                return ApiResponse.error("Tài khoản này đã liên kết với một nhân viên khác");
            }
        } else {
            return ApiResponse.error("Bắt buộc liên kết với một tài khoản người dùng");
        }

        Employee employee = Employee.builder()
                .user(user)
                .fullName(request.getFullName())
                .position(request.getPosition())
                .salary(request.getSalary())
                .hireDate(request.getHireDate())
                .status(EmployeeStatus.WORKING)
                .build();

        Employee savedEmployee = employeeRepository.save(employee);
        log.info("Nhân viên mới được thêm: id='{}', fullName='{}'", savedEmployee.getId(), savedEmployee.getFullName());
        return ApiResponse.success("Thêm nhân viên thành công", mapToEmployeeResponse(savedEmployee));
    }

    @Override
    @Transactional
    public ApiResponse<EmployeeResponse> updateEmployee(Long id, UpdateEmployeeRequest request) {
        Employee employee = employeeRepository.findById(id).orElse(null);
        if (employee == null) {
            return ApiResponse.error("Không tìm thấy nhân viên");
        }

        if (request.getStatus() == null) {
            return ApiResponse.error("Trạng thái làm việc không được để trống");
        }
        EmployeeStatus status;
        try {
            status = EmployeeStatus.valueOf(request.getStatus().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ApiResponse.error("Trạng thái làm việc không hợp lệ");
        }

        employee.setFullName(request.getFullName());
        employee.setPosition(request.getPosition());
        employee.setSalary(request.getSalary());
        employee.setHireDate(request.getHireDate());
        employee.setStatus(status);

        Employee updatedEmployee = employeeRepository.save(employee);
        log.info("Nhân viên cập nhật: id='{}', fullName='{}'", updatedEmployee.getId(), updatedEmployee.getFullName());
        return ApiResponse.success("Cập nhật nhân viên thành công", mapToEmployeeResponse(updatedEmployee));
    }

    @Override
    @Transactional
    public ApiResponse<Void> deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id).orElse(null);
        if (employee == null) {
            return ApiResponse.error("Không tìm thấy nhân viên");
        }

        // Integrity check: Orders
        if (orderRepository.existsByEmployeeId(id)) {
            return ApiResponse.error("Không thể xóa nhân viên này vì đã có dữ liệu giao dịch liên quan (Hóa đơn/Thanh toán).");
        }

        // Integrity check: Payments
        if (paymentRepository.existsByCashierId(id)) {
            return ApiResponse.error("Không thể xóa nhân viên này vì đã có dữ liệu giao dịch liên quan (Hóa đơn/Thanh toán).");
        }

        employeeRepository.delete(employee);
        log.info("Nhân viên bị xóa khỏi hệ thống: id='{}', fullName='{}'", id, employee.getFullName());
        return ApiResponse.success("Xóa nhân viên thành công");
    }

    @Override
    @Transactional
    public ApiResponse<EmployeeResponse> updatePosition(Long id, UpdatePositionRequest request) {
        Employee employee = employeeRepository.findById(id).orElse(null);
        if (employee == null) {
            return ApiResponse.error("Không tìm thấy nhân viên");
        }

        employee.setPosition(request.getPosition());
        Employee updated = employeeRepository.save(employee);
        log.info("Cập nhật vị trí nhân viên: id='{}', position='{}'", id, request.getPosition());
        return ApiResponse.success("Cập nhật vị trí làm việc thành công", mapToEmployeeResponse(updated));
    }

    @Override
    @Transactional
    public ApiResponse<EmployeeResponse> updateSalary(Long id, UpdateSalaryRequest request) {
        Employee employee = employeeRepository.findById(id).orElse(null);
        if (employee == null) {
            return ApiResponse.error("Không tìm thấy nhân viên");
        }

        employee.setSalary(request.getSalary());
        Employee updated = employeeRepository.save(employee);
        log.info("Cập nhật lương nhân viên: id='{}', salary='{}'", id, request.getSalary());
        return ApiResponse.success("Cập nhật lương thành công", mapToEmployeeResponse(updated));
    }

    private EmployeeResponse mapToEmployeeResponse(Employee employee) {
        if (employee == null) return null;
        return EmployeeResponse.builder()
                .id(employee.getId())
                .fullName(employee.getFullName())
                .position(employee.getPosition())
                .salary(employee.getSalary())
                .hireDate(employee.getHireDate())
                .status(employee.getStatus().name())
                .userId(employee.getUser() != null ? employee.getUser().getId() : null)
                .username(employee.getUser() != null ? employee.getUser().getUsername() : null)
                .build();
    }
}
