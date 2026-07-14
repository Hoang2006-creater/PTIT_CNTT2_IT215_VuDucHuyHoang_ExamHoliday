package com.re.examholiday.controller;

import com.re.examholiday.dto.response.ApiResponse;
import com.re.examholiday.model.Customer;
import com.re.examholiday.model.Role;
import com.re.examholiday.model.User;
import com.re.examholiday.model.enumeration.MembershipClass;
import com.re.examholiday.model.enumeration.RoleName;
import com.re.examholiday.model.enumeration.UserStatus;
import com.re.examholiday.repository.CustomerRepository;
import com.re.examholiday.repository.RoleRepository;
import com.re.examholiday.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class RegisterRestController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@RequestBody RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Tên đăng nhập đã tồn tại"));
        }
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty() && userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Email đã tồn tại"));
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Số điện thoại đã tồn tại"));
        }

        Role customerRole = roleRepository.findByName(RoleName.CUSTOMER)
                .orElseGet(() -> {
                    Role newRole = Role.builder()
                            .name(RoleName.CUSTOMER)
                            .description("Customer role")
                            .build();
                    return roleRepository.save(newRole);
                });

        User user = User.builder()
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .phone(request.getPhone())
                .role(customerRole)
                .status(UserStatus.ACTIVE)
                .failedLoginAttempts(0)
                .build();

        User savedUser = userRepository.save(user);

        Customer customer = Customer.builder()
                .user(savedUser)
                .fullName(request.getUsername())
                .phone(request.getPhone())
                .loyaltyPoints(0)
                .membershipClass(MembershipClass.BRONZE)
                .build();

        customerRepository.save(customer);

        return ResponseEntity.ok(ApiResponse.success("Đăng ký tài khoản thành công"));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterRequest {
        private String username;
        private String password;
        private String email;
        private String phone;
    }
}
