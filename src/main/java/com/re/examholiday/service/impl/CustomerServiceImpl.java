package com.re.examholiday.service.impl;

import com.re.examholiday.dto.request.CreateCustomerRequest;
import com.re.examholiday.dto.request.UpdateCustomerRequest;
import com.re.examholiday.dto.request.UpdateLoyaltyPointsRequest;
import com.re.examholiday.dto.response.ApiResponse;
import com.re.examholiday.dto.response.CustomerOrderResponse;
import com.re.examholiday.dto.response.CustomerResponse;
import com.re.examholiday.dto.response.LoyaltyPointHistoryResponse;
import com.re.examholiday.model.Customer;
import com.re.examholiday.model.Order;
import com.re.examholiday.model.User;
import com.re.examholiday.model.enumeration.MembershipClass;
import com.re.examholiday.model.enumeration.OrderStatus;
import com.re.examholiday.repository.CustomerRepository;
import com.re.examholiday.repository.OrderRepository;
import com.re.examholiday.repository.ReservationRepository;
import com.re.examholiday.repository.UserRepository;
import com.re.examholiday.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ReservationRepository reservationRepository;

    @Override
    public ApiResponse<List<CustomerResponse>> getAllCustomers() {
        List<Customer> customers = customerRepository.findAll();
        List<CustomerResponse> responseList = customers.stream()
                .map(this::mapToCustomerResponse)
                .collect(Collectors.toList());
        return ApiResponse.success("Lấy danh sách khách hàng thành công", responseList);
    }

    @Override
    public ApiResponse<CustomerResponse> getCustomerDetail(Long id) {
        Customer customer = customerRepository.findById(id).orElse(null);
        if (customer == null) {
            return ApiResponse.error("Không tìm thấy khách hàng");
        }
        return ApiResponse.success("Lấy chi tiết khách hàng thành công", mapToCustomerResponse(customer));
    }

    @Override
    @Transactional
    public ApiResponse<CustomerResponse> createCustomer(CreateCustomerRequest request) {
        if (customerRepository.findByPhone(request.getPhone()).isPresent()) {
            return ApiResponse.error("Số điện thoại khách hàng đã tồn tại");
        }

        User user = null;
        if (request.getUserId() != null) {
            user = userRepository.findById(request.getUserId()).orElse(null);
            if (user == null) {
                return ApiResponse.error("Không tìm thấy tài khoản để liên kết");
            }
            if (customerRepository.findByUserId(request.getUserId()).isPresent()) {
                return ApiResponse.error("Tài khoản này đã liên kết với một khách hàng khác");
            }
        }

        MembershipClass membershipClass;
        try {
            membershipClass = MembershipClass.valueOf(request.getMembershipClass().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ApiResponse.error("Hạng thành viên không hợp lệ");
        }

        Customer customer = Customer.builder()
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .loyaltyPoints(request.getLoyaltyPoints())
                .membershipClass(membershipClass)
                .user(user)
                .build();

        // Auto determine membership class based on points if not explicitly silver/gold/platinum or to enforce rules
        customer.setMembershipClass(determineMembershipClass(request.getLoyaltyPoints()));

        Customer savedCustomer = customerRepository.save(customer);
        log.info("Khách hàng mới được thêm: id='{}', fullName='{}'", savedCustomer.getId(), savedCustomer.getFullName());
        return ApiResponse.success("Thêm khách hàng thành công", mapToCustomerResponse(savedCustomer));
    }

    @Override
    @Transactional
    public ApiResponse<CustomerResponse> updateCustomer(Long id, UpdateCustomerRequest request) {
        Customer customer = customerRepository.findById(id).orElse(null);
        if (customer == null) {
            return ApiResponse.error("Không tìm thấy khách hàng");
        }

        if (request.getPhone() != null && !request.getPhone().trim().isEmpty() && !request.getPhone().equals(customer.getPhone())) {
            if (customerRepository.findByPhone(request.getPhone()).isPresent()) {
                return ApiResponse.error("Số điện thoại đã tồn tại ở khách hàng khác");
            }
        }

        MembershipClass membershipClass = determineMembershipClass(request.getLoyaltyPoints());

        customer.setFullName(request.getFullName());
        customer.setPhone(request.getPhone());
        customer.setLoyaltyPoints(request.getLoyaltyPoints());
        customer.setMembershipClass(membershipClass);

        Customer updatedCustomer = customerRepository.save(customer);
        log.info("Khách hàng cập nhật: id='{}', fullName='{}'", updatedCustomer.getId(), updatedCustomer.getFullName());
        return ApiResponse.success("Cập nhật khách hàng thành công", mapToCustomerResponse(updatedCustomer));
    }

    @Override
    @Transactional
    public ApiResponse<Void> deleteCustomer(Long id) {
        Customer customer = customerRepository.findById(id).orElse(null);
        if (customer == null) {
            return ApiResponse.error("Không tìm thấy khách hàng");
        }

        // Check active reservations or orders
        if (reservationRepository.existsByCustomerId(id)) {
            return ApiResponse.error("Không thể xóa khách hàng vì đã có lịch sử đặt bàn trong hệ thống.");
        }

        List<Order> orders = orderRepository.findByCustomerId(id);
        if (orders != null && !orders.isEmpty()) {
            return ApiResponse.error("Không thể xóa khách hàng này vì đã có dữ liệu giao dịch hóa đơn liên quan.");
        }

        customerRepository.delete(customer);
        log.info("Khách hàng bị xóa: id='{}', fullName='{}'", id, customer.getFullName());
        return ApiResponse.success("Xóa khách hàng thành công");
    }

    @Override
    @Transactional
    public ApiResponse<CustomerResponse> updateLoyaltyPoints(Long id, UpdateLoyaltyPointsRequest request) {
        Customer customer = customerRepository.findById(id).orElse(null);
        if (customer == null) {
            return ApiResponse.error("Không tìm thấy khách hàng");
        }

        customer.setLoyaltyPoints(request.getLoyaltyPoints());
        customer.setMembershipClass(determineMembershipClass(request.getLoyaltyPoints()));

        Customer updatedCustomer = customerRepository.save(customer);
        log.info("Cập nhật điểm tích lũy khách hàng: id='{}', points='{}', class='{}'", 
                id, request.getLoyaltyPoints(), updatedCustomer.getMembershipClass());
        return ApiResponse.success("Cập nhật điểm tích lũy thành công", mapToCustomerResponse(updatedCustomer));
    }

    @Override
    public ApiResponse<List<CustomerOrderResponse>> getCustomerOrderHistory(Long id) {
        Customer customer = customerRepository.findById(id).orElse(null);
        if (customer == null) {
            return ApiResponse.error("Không tìm thấy khách hàng");
        }

        List<Order> orders = orderRepository.findByCustomerId(id);
        if (orders == null || orders.isEmpty()) {
            return ApiResponse.success("Khách hàng chưa có lịch sử gọi món", Collections.emptyList());
        }

        List<CustomerOrderResponse> history = orders.stream()
                .map(order -> CustomerOrderResponse.builder()
                        .id(order.getId())
                        .orderTime(order.getOrderTime())
                        .totalAmount(order.getTotalAmount())
                        .status(order.getStatus().name())
                        .tableNumber(order.getTable() != null ? order.getTable().getTableNumber() : "Không xác định")
                        .employeeName(order.getEmployee() != null ? order.getEmployee().getFullName() : "Không xác định")
                        .build())
                .collect(Collectors.toList());

        return ApiResponse.success("Lấy lịch sử gọi món thành công", history);
    }

    private CustomerResponse mapToCustomerResponse(Customer customer) {
        if (customer == null) return null;
        
        String email = customer.getUser() != null ? customer.getUser().getEmail() : null;
        
        java.math.BigDecimal totalSpent = java.math.BigDecimal.ZERO;
        List<Order> customerOrders = orderRepository.findByCustomerId(customer.getId());
        if (customerOrders != null) {
            totalSpent = customerOrders.stream()
                    .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
                    .map(Order::getTotalAmount)
                    .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        }

        return CustomerResponse.builder()
                .id(customer.getId())
                .fullName(customer.getFullName())
                .phone(customer.getPhone())
                .loyaltyPoints(customer.getLoyaltyPoints())
                .membershipClass(customer.getMembershipClass().name())
                .userId(customer.getUser() != null ? customer.getUser().getId() : null)
                .username(customer.getUser() != null ? customer.getUser().getUsername() : null)
                .email(email)
                .totalSpent(totalSpent)
                .build();
    }

    private MembershipClass determineMembershipClass(int points) {
        if (points >= 1000) {
            return MembershipClass.PLATINUM;
        } else if (points >= 500) {
            return MembershipClass.GOLD;
        } else if (points >= 100) {
            return MembershipClass.SILVER;
        } else {
            return MembershipClass.BRONZE;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<CustomerResponse> getCustomerLoyaltyInfo(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return ApiResponse.error("Không tìm thấy tài khoản");
        }
        Customer customer = customerRepository.findByUserId(user.getId()).orElse(null);
        if (customer == null) {
            return ApiResponse.error("Tài khoản chưa liên kết thông tin khách hàng.");
        }
        return ApiResponse.success("Lấy thông tin thành viên thành công", mapToCustomerResponse(customer));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<LoyaltyPointHistoryResponse>> getLoyaltyPointHistory(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return ApiResponse.error("Không tìm thấy tài khoản");
        }
        Customer customer = customerRepository.findByUserId(user.getId()).orElse(null);
        if (customer == null) {
            return ApiResponse.error("Tài khoản chưa liên kết thông tin khách hàng.");
        }

        // Query COMPLETED orders of this customer
        List<Order> orders = orderRepository.findAll().stream()
                .filter(o -> o.getCustomer() != null && o.getCustomer().getId().equals(customer.getId()) && o.getStatus() == OrderStatus.COMPLETED)
                .sorted(Comparator.comparing(Order::getOrderTime).reversed())
                .collect(Collectors.toList());

        List<LoyaltyPointHistoryResponse> history = orders.stream().map(order -> {
            int points = order.getTotalAmount().divide(java.math.BigDecimal.valueOf(10000)).intValue();
            return LoyaltyPointHistoryResponse.builder()
                    .orderId(order.getId())
                    .transactionTime(order.getOrderTime())
                    .description("Tích điểm từ hóa đơn #" + order.getId())
                    .pointsChange(points)
                    .type("EARNED")
                    .build();
        }).collect(Collectors.toList());

        return ApiResponse.success("Lấy lịch sử tích điểm thành công", history);
    }
}
