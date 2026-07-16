package com.re.examholiday.service.impl;

import com.re.examholiday.dto.request.CreateReviewRequest;
import com.re.examholiday.dto.request.UpdateReviewRequest;
import com.re.examholiday.dto.response.ApiResponse;
import com.re.examholiday.model.Customer;
import com.re.examholiday.model.Order;
import com.re.examholiday.model.Review;
import com.re.examholiday.model.User;
import com.re.examholiday.model.enumeration.OrderStatus;
import com.re.examholiday.repository.CustomerRepository;
import com.re.examholiday.repository.OrderRepository;
import com.re.examholiday.repository.ReviewRepository;
import com.re.examholiday.repository.UserRepository;
import com.re.examholiday.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;

    @Override
    @Transactional
    public ApiResponse<Review> createReview(String username, CreateReviewRequest request) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return ApiResponse.error("Không tìm thấy tài khoản");
        }

        Customer customer = customerRepository.findByUserId(user.getId()).orElse(null);
        if (customer == null) {
            return ApiResponse.error("Tài khoản chưa liên kết hồ sơ khách hàng. Vui lòng thiết lập hồ sơ trước.");
        }

        Order order = orderRepository.findById(request.getOrderId()).orElse(null);
        if (order == null) {
            return ApiResponse.error("Đơn hàng không tồn tại");
        }

        // Verify order belongs to the customer
        if (order.getCustomer() == null || !order.getCustomer().getId().equals(customer.getId())) {
            return ApiResponse.error("Bạn không thể đánh giá đơn hàng của khách hàng khác.");
        }

        // Verify order is completed
        if (order.getStatus() != OrderStatus.COMPLETED) {
            return ApiResponse.error("Bạn chỉ có thể đánh giá các hóa đơn đã được hoàn thành.");
        }

        // Verify order has not been reviewed yet
        if (reviewRepository.existsByOrderId(request.getOrderId())) {
            return ApiResponse.error("Đơn hàng này đã được đánh giá.");
        }

        Review review = Review.builder()
                .customer(customer)
                .order(order)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        Review saved = reviewRepository.save(review);
        log.info("Khách hàng đánh giá đơn hàng thành công: customer='{}', orderId='{}', rating='{}'", customer.getFullName(), request.getOrderId(), request.getRating());
        return ApiResponse.success("Đánh giá đơn hàng thành công", saved);
    }

    @Override
    @Transactional
    public ApiResponse<Review> updateReview(String username, Long reviewId, UpdateReviewRequest request) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return ApiResponse.error("Không tìm thấy tài khoản");
        }

        Customer customer = customerRepository.findByUserId(user.getId()).orElse(null);
        if (customer == null) {
            return ApiResponse.error("Tài khoản chưa liên kết hồ sơ khách hàng.");
        }

        Review review = reviewRepository.findById(reviewId).orElse(null);
        if (review == null) {
            return ApiResponse.error("Đánh giá không tồn tại");
        }

        // Verify review belongs to the customer
        if (review.getCustomer() == null || !review.getCustomer().getId().equals(customer.getId())) {
            return ApiResponse.error("Bạn không có quyền chỉnh sửa đánh giá của tài khoản khác.");
        }

        review.setRating(request.getRating());
        review.setComment(request.getComment());

        Review saved = reviewRepository.save(review);
        log.info("Cập nhật đánh giá thành công: customer='{}', reviewId='{}', rating='{}'", customer.getFullName(), reviewId, request.getRating());
        return ApiResponse.success("Cập nhật đánh giá thành công", saved);
    }

    @Override
    @Transactional
    public ApiResponse<Void> deleteReview(String username, Long reviewId) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return ApiResponse.error("Không tìm thấy tài khoản");
        }

        Customer customer = customerRepository.findByUserId(user.getId()).orElse(null);
        if (customer == null) {
            return ApiResponse.error("Tài khoản chưa liên kết hồ sơ khách hàng.");
        }

        Review review = reviewRepository.findById(reviewId).orElse(null);
        if (review == null) {
            return ApiResponse.error("Đánh giá không tồn tại");
        }

        // Verify review belongs to the customer
        if (review.getCustomer() == null || !review.getCustomer().getId().equals(customer.getId())) {
            return ApiResponse.error("Bạn không có quyền xóa đánh giá của tài khoản khác.");
        }

        reviewRepository.delete(review);
        log.info("Xóa đánh giá thành công: customer='{}', reviewId='{}'", customer.getFullName(), reviewId);
        return ApiResponse.success("Xóa đánh giá thành công");
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<Review>> getMyReviews(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return ApiResponse.error("Không tìm thấy tài khoản");
        }

        Customer customer = customerRepository.findByUserId(user.getId()).orElse(null);
        if (customer == null) {
            return ApiResponse.error("Tài khoản chưa liên kết hồ sơ khách hàng.");
        }

        List<Review> reviews = reviewRepository.findByCustomerId(customer.getId());
        return ApiResponse.success("Lấy danh sách đánh giá cá nhân thành công", reviews);
    }
}
