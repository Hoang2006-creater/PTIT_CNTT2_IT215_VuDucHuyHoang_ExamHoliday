package com.re.examholiday.service.impl;

import com.re.examholiday.dto.response.ApiResponse;
import com.re.examholiday.model.Category;
import com.re.examholiday.model.MenuItem;
import com.re.examholiday.repository.CategoryRepository;
import com.re.examholiday.repository.MenuItemRepository;
import com.re.examholiday.service.CloudinaryService;
import com.re.examholiday.service.MenuItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuItemServiceImpl implements MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final CategoryRepository categoryRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    public ApiResponse<List<MenuItem>> getAllMenuItems() {
        List<MenuItem> items = menuItemRepository.findAll();
        return ApiResponse.success("Lấy danh sách món ăn thành công", items);
    }

    @Override
    public ApiResponse<MenuItem> getMenuItemDetail(Long id) {
        MenuItem item = menuItemRepository.findById(id).orElse(null);
        if (item == null) {
            return ApiResponse.error("Không tìm thấy món ăn");
        }
        return ApiResponse.success("Lấy chi tiết món ăn thành công", item);
    }

    @Override
    @Transactional
    public ApiResponse<MenuItem> createMenuItem(String name, String description, BigDecimal price, Integer categoryId, Boolean available, MultipartFile image) {
        if (menuItemRepository.findByName(name).isPresent()) {
            return ApiResponse.error("Tên món ăn đã tồn tại");
        }

        Category category = categoryRepository.findById(categoryId).orElse(null);
        if (category == null) {
            return ApiResponse.error("Không tìm thấy danh mục");
        }

        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            try {
                imageUrl = cloudinaryService.uploadFile(image);
            } catch (IOException e) {
                log.error("Lỗi khi upload ảnh lên Cloudinary: ", e);
                return ApiResponse.error("Không thể tải lên hình ảnh món ăn: " + e.getMessage());
            }
        }

        MenuItem menuItem = MenuItem.builder()
                .name(name)
                .description(description)
                .price(price)
                .category(category)
                .isAvailable(available != null ? available : true)
                .imageUrl(imageUrl)
                .build();

        MenuItem saved = menuItemRepository.save(menuItem);
        return ApiResponse.success("Thêm món ăn thành công", saved);
    }

    @Override
    @Transactional
    public ApiResponse<MenuItem> updateMenuItem(Long id, String name, String description, BigDecimal price, Integer categoryId, Boolean available, MultipartFile image) {
        MenuItem menuItem = menuItemRepository.findById(id).orElse(null);
        if (menuItem == null) {
            return ApiResponse.error("Không tìm thấy món ăn");
        }

        Optional<MenuItem> existingWithName = menuItemRepository.findByName(name);
        if (existingWithName.isPresent() && !existingWithName.get().getId().equals(id)) {
            return ApiResponse.error("Tên món ăn đã tồn tại");
        }

        Category category = categoryRepository.findById(categoryId).orElse(null);
        if (category == null) {
            return ApiResponse.error("Không tìm thấy danh mục");
        }

        menuItem.setName(name);
        menuItem.setDescription(description);
        menuItem.setPrice(price);
        menuItem.setCategory(category);
        menuItem.setIsAvailable(available != null ? available : true);

        if (image != null && !image.isEmpty()) {
            try {
                String newImageUrl = cloudinaryService.uploadFile(image);
                if (menuItem.getImageUrl() != null && !menuItem.getImageUrl().isEmpty()) {
                    String oldPublicId = cloudinaryService.getPublicId(menuItem.getImageUrl());
                    if (oldPublicId != null) {
                        try {
                            cloudinaryService.deleteFile(oldPublicId);
                        } catch (IOException e) {
                            log.warn("Không thể xóa ảnh cũ trên Cloudinary: " + oldPublicId, e);
                        }
                    }
                }
                menuItem.setImageUrl(newImageUrl);
            } catch (IOException e) {
                log.error("Lỗi khi upload ảnh lên Cloudinary: ", e);
                return ApiResponse.error("Không thể tải lên hình ảnh món ăn mới: " + e.getMessage());
            }
        }

        MenuItem saved = menuItemRepository.save(menuItem);
        return ApiResponse.success("Cập nhật món ăn thành công", saved);
    }

    @Override
    @Transactional
    public ApiResponse<Void> deleteMenuItem(Long id) {
        MenuItem menuItem = menuItemRepository.findById(id).orElse(null);
        if (menuItem == null) {
            return ApiResponse.error("Không tìm thấy món ăn");
        }

        if (menuItem.getImageUrl() != null && !menuItem.getImageUrl().isEmpty()) {
            String publicId = cloudinaryService.getPublicId(menuItem.getImageUrl());
            if (publicId != null) {
                try {
                    cloudinaryService.deleteFile(publicId);
                } catch (IOException e) {
                    log.warn("Không thể xóa ảnh trên Cloudinary khi xóa món ăn: " + publicId, e);
                }
            }
        }

        menuItemRepository.delete(menuItem);
        return ApiResponse.success("Xóa món ăn thành công");
    }
}
