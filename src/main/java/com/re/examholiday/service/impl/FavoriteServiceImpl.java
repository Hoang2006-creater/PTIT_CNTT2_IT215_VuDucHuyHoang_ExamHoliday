package com.re.examholiday.service.impl;

import com.re.examholiday.dto.request.FavoriteRequest;
import com.re.examholiday.dto.response.ApiResponse;
import com.re.examholiday.dto.response.CustomerFavoriteResponse;
import com.re.examholiday.model.Customer;
import com.re.examholiday.model.Favorite;
import com.re.examholiday.model.MenuItem;
import com.re.examholiday.model.User;
import com.re.examholiday.repository.CustomerRepository;
import com.re.examholiday.repository.FavoriteRepository;
import com.re.examholiday.repository.MenuItemRepository;
import com.re.examholiday.repository.UserRepository;
import com.re.examholiday.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FavoriteServiceImpl implements FavoriteService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final MenuItemRepository menuItemRepository;
    private final FavoriteRepository favoriteRepository;

    @Override
    @Transactional
    public ApiResponse<Favorite> addFavorite(String username, FavoriteRequest request) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return ApiResponse.error("Không tìm thấy tài khoản");
        }

        Customer customer = customerRepository.findByUserId(user.getId()).orElse(null);
        if (customer == null) {
            return ApiResponse.error("Tài khoản chưa liên kết hồ sơ khách hàng. Vui lòng thiết lập hồ sơ trước.");
        }

        MenuItem menuItem = menuItemRepository.findById(request.getMenuItemId()).orElse(null);
        if (menuItem == null) {
            return ApiResponse.error("Món ăn không tồn tại");
        }

        // Check duplicate
        if (favoriteRepository.existsByCustomerIdAndMenuItemId(customer.getId(), request.getMenuItemId())) {
            return ApiResponse.error("Món ăn này đã có trong danh sách yêu thích của bạn.");
        }

        Favorite favorite = Favorite.builder()
                .customer(customer)
                .menuItem(menuItem)
                .build();

        Favorite saved = favoriteRepository.save(favorite);
        log.info("Thêm món yêu thích thành công: customer='{}', menuItem='{}'", customer.getFullName(), menuItem.getName());
        return ApiResponse.success("Đã thêm món vào danh sách yêu thích", saved);
    }

    @Override
    @Transactional
    public ApiResponse<Void> removeFavorite(String username, Long menuItemId) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return ApiResponse.error("Không tìm thấy tài khoản");
        }

        Customer customer = customerRepository.findByUserId(user.getId()).orElse(null);
        if (customer == null) {
            return ApiResponse.error("Tài khoản chưa liên kết hồ sơ khách hàng.");
        }

        Favorite favorite = favoriteRepository.findByCustomerIdAndMenuItemId(customer.getId(), menuItemId).orElse(null);
        if (favorite == null) {
            return ApiResponse.error("Món ăn này chưa có trong danh sách yêu thích của bạn.");
        }

        favoriteRepository.delete(favorite);
        log.info("Xóa món yêu thích thành công: customer='{}', menuItemId='{}'", customer.getFullName(), menuItemId);
        return ApiResponse.success("Đã xóa món khỏi danh sách yêu thích");
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<CustomerFavoriteResponse>> getFavorites(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return ApiResponse.error("Không tìm thấy tài khoản");
        }

        Customer customer = customerRepository.findByUserId(user.getId()).orElse(null);
        if (customer == null) {
            return ApiResponse.error("Tài khoản chưa liên kết hồ sơ khách hàng.");
        }

        List<Favorite> favorites = favoriteRepository.findByCustomerId(customer.getId());
        List<CustomerFavoriteResponse> responses = favorites.stream().map(f -> {
            MenuItem item = f.getMenuItem();
            return CustomerFavoriteResponse.builder()
                    .favoriteId(f.getId())
                    .menuItemId(item.getId())
                    .name(item.getName())
                    .description(item.getDescription())
                    .price(item.getPrice())
                    .imageUrl(item.getImageUrl())
                    .isAvailable(item.getIsAvailable())
                    .categoryId(item.getCategory() != null ? item.getCategory().getId() : null)
                    .categoryName(item.getCategory() != null ? item.getCategory().getName() : null)
                    .build();
        }).collect(Collectors.toList());

        return ApiResponse.success("Lấy danh sách món yêu thích thành công", responses);
    }
}
