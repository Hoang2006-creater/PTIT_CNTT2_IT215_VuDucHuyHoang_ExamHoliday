package com.re.examholiday.service;

import com.re.examholiday.dto.request.ChangePasswordRequest;
import com.re.examholiday.dto.request.CreateUserRequest;
import com.re.examholiday.dto.request.ResetPasswordRequest;
import com.re.examholiday.dto.request.UpdateUserRequest;
import com.re.examholiday.dto.response.ApiResponse;
import com.re.examholiday.dto.response.UserResponse;

import java.util.List;

public interface UserManageService {

    ApiResponse<List<UserResponse>> getAllUsers();

    ApiResponse<UserResponse> getUserDetail(Long id);

    ApiResponse<UserResponse> createUser(CreateUserRequest request);

    ApiResponse<UserResponse> updateUser(Long id, UpdateUserRequest request);

    ApiResponse<Void> lockUser(Long id);

    ApiResponse<Void> unlockUser(Long id);

    ApiResponse<Void> resetPassword(Long id, ResetPasswordRequest request);

    ApiResponse<Void> changePassword(String currentUsername, ChangePasswordRequest request);
}
