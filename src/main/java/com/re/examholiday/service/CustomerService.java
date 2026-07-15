package com.re.examholiday.service;

import com.re.examholiday.dto.request.CreateCustomerRequest;
import com.re.examholiday.dto.request.UpdateCustomerRequest;
import com.re.examholiday.dto.request.UpdateLoyaltyPointsRequest;
import com.re.examholiday.dto.response.ApiResponse;
import com.re.examholiday.dto.response.CustomerOrderResponse;
import com.re.examholiday.dto.response.CustomerResponse;

import java.util.List;

public interface CustomerService {

    ApiResponse<List<CustomerResponse>> getAllCustomers();

    ApiResponse<CustomerResponse> getCustomerDetail(Long id);

    ApiResponse<CustomerResponse> createCustomer(CreateCustomerRequest request);

    ApiResponse<CustomerResponse> updateCustomer(Long id, UpdateCustomerRequest request);

    ApiResponse<Void> deleteCustomer(Long id);

    ApiResponse<CustomerResponse> updateLoyaltyPoints(Long id, UpdateLoyaltyPointsRequest request);

    ApiResponse<List<CustomerOrderResponse>> getCustomerOrderHistory(Long id);
}
