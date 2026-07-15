package com.re.examholiday.service.impl;

import com.re.examholiday.dto.request.ChangePasswordRequest;
import com.re.examholiday.dto.request.CreateUserRequest;
import com.re.examholiday.dto.request.ResetPasswordRequest;
import com.re.examholiday.dto.request.UpdateUserRequest;
import com.re.examholiday.dto.response.ApiResponse;
import com.re.examholiday.dto.response.RoleDto;
import com.re.examholiday.dto.response.UserResponse;
import com.re.examholiday.model.Role;
import com.re.examholiday.model.User;
import com.re.examholiday.model.enumeration.RoleName;
import com.re.examholiday.model.enumeration.UserStatus;
import com.re.examholiday.repository.RoleRepository;
import com.re.examholiday.repository.UserRepository;
import com.re.examholiday.service.UserManageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserManageServiceImpl implements UserManageService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public ApiResponse<List<UserResponse>> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserResponse> responseList = users.stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
        return ApiResponse.success("Lấy danh sách tài khoản thành công", responseList);
    }

    @Override
    public ApiResponse<UserResponse> getUserDetail(Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ApiResponse.error("Không tìm thấy tài khoản");
        }
        return ApiResponse.success("Lấy chi tiết tài khoản thành công", mapToUserResponse(user));
    }

    @Override
    @Transactional
    public ApiResponse<UserResponse> createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return ApiResponse.error("Tên đăng nhập đã tồn tại");
        }
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            if (userRepository.existsByEmail(request.getEmail())) {
                return ApiResponse.error("Email đã tồn tại");
            }
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            return ApiResponse.error("Số điện thoại đã tồn tại");
        }

        RoleName roleName = getRoleNameFromId(request.getRoleId());
        if (roleName == null) {
            return ApiResponse.error("Quyền hạn không hợp lệ");
        }

        Role role = roleRepository.findByName(roleName)
                .orElseGet(() -> {
                    Role newRole = Role.builder()
                            .name(roleName)
                            .description(roleName.name() + " role")
                            .build();
                    return roleRepository.save(newRole);
                });

        User user = User.builder()
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .phone(request.getPhone())
                .role(role)
                .status(UserStatus.ACTIVE)
                .failedLoginAttempts(0)
                .build();

        User savedUser = userRepository.save(user);
        log.info("Tài khoản mới được tạo: username='{}', role='{}'", savedUser.getUsername(), savedUser.getRole().getName());
        return ApiResponse.success("Tạo tài khoản thành công", mapToUserResponse(savedUser));
    }

    @Override
    @Transactional
    public ApiResponse<UserResponse> updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ApiResponse.error("Không tìm thấy tài khoản");
        }

        if (request.getEmail() != null && !request.getEmail().trim().isEmpty() && !request.getEmail().equalsIgnoreCase(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                return ApiResponse.error("Email đã tồn tại");
            }
        }

        if (request.getPhone() != null && !request.getPhone().trim().isEmpty() && !request.getPhone().equals(user.getPhone())) {
            if (userRepository.existsByPhone(request.getPhone())) {
                return ApiResponse.error("Số điện thoại đã tồn tại");
            }
        }

        RoleName roleName = getRoleNameFromId(request.getRoleId());
        if (roleName == null) {
            return ApiResponse.error("Quyền hạn không hợp lệ");
        }

        Role role = roleRepository.findByName(roleName)
                .orElseGet(() -> {
                    Role newRole = Role.builder()
                            .name(roleName)
                            .description(roleName.name() + " role")
                            .build();
                    return roleRepository.save(newRole);
                });

        if (request.getStatus() == null) {
            return ApiResponse.error("Trạng thái không được để trống");
        }
        UserStatus status;
        try {
            status = UserStatus.valueOf(request.getStatus().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ApiResponse.error("Trạng thái không hợp lệ");
        }

        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setRole(role);
        user.setStatus(status);

        if (status == UserStatus.ACTIVE) {
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
        } else if (status == UserStatus.BLOCKED) {
            user.setLockedUntil(null);
        }

        User updatedUser = userRepository.save(user);
        log.info("Tài khoản cập nhật: id='{}', username='{}'", updatedUser.getId(), updatedUser.getUsername());
        return ApiResponse.success("Cập nhật tài khoản thành công", mapToUserResponse(updatedUser));
    }

    @Override
    @Transactional
    public ApiResponse<Void> lockUser(Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ApiResponse.error("Không tìm thấy tài khoản");
        }
        user.setStatus(UserStatus.BLOCKED);
        userRepository.save(user);
        log.info("Tài khoản bị khóa bởi ADMIN: id='{}', username='{}'", user.getId(), user.getUsername());
        return ApiResponse.success("Khóa tài khoản thành công");
    }

    @Override
    @Transactional
    public ApiResponse<Void> unlockUser(Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ApiResponse.error("Không tìm thấy tài khoản");
        }
        user.setStatus(UserStatus.ACTIVE);
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);
        log.info("Tài khoản được mở khóa bởi ADMIN: id='{}', username='{}'", user.getId(), user.getUsername());
        return ApiResponse.success("Mở khóa tài khoản thành công");
    }

    @Override
    @Transactional
    public ApiResponse<Void> resetPassword(Long id, ResetPasswordRequest request) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ApiResponse.error("Không tìm thấy tài khoản");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Mật khẩu tài khoản đã được reset bởi ADMIN: id='{}', username='{}'", user.getId(), user.getUsername());
        return ApiResponse.success("Đặt lại mật khẩu thành công");
    }

    @Override
    @Transactional
    public ApiResponse<Void> changePassword(String currentUsername, ChangePasswordRequest request) {
        User user = userRepository.findByUsername(currentUsername).orElse(null);
        if (user == null) {
            return ApiResponse.error("Không tìm thấy tài khoản");
        }

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            return ApiResponse.error("Mật khẩu cũ không chính xác");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Người dùng tự đổi mật khẩu thành công: username='{}'", currentUsername);
        return ApiResponse.success("Đổi mật khẩu thành công");
    }

    private UserResponse mapToUserResponse(User user) {
        if (user == null) return null;
        RoleDto roleDto = null;
        if (user.getRole() != null) {
            roleDto = RoleDto.builder()
                    .id(user.getRole().getId())
                    .name(user.getRole().getName().name())
                    .description(user.getRole().getDescription())
                    .build();
        }
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(roleDto)
                .status(user.getStatus().name())
                .build();
    }

    private RoleName getRoleNameFromId(Integer id) {
        if (id == null) return null;
        return switch (id) {
            case 1 -> RoleName.ADMIN;
            case 2 -> RoleName.MANAGER;
            case 3 -> RoleName.WAITSTAFF;
            case 4 -> RoleName.CASHIER;
            case 5 -> RoleName.CHEF;
            case 6 -> RoleName.CUSTOMER;
            default -> null;
        };
    }
}
