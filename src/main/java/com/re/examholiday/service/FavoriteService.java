package com.re.examholiday.service;

import com.re.examholiday.dto.request.FavoriteRequest;
import com.re.examholiday.dto.response.ApiResponse;
import com.re.examholiday.dto.response.CustomerFavoriteResponse;
import com.re.examholiday.model.Favorite;

import java.util.List;

public interface FavoriteService {
    ApiResponse<Favorite> addFavorite(String username, FavoriteRequest request);
    ApiResponse<Void> removeFavorite(String username, Long menuItemId);
    ApiResponse<List<CustomerFavoriteResponse>> getFavorites(String username);
}
