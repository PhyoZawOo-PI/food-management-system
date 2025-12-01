package com.phyo.food_management_system.service;

import com.phyo.food_management_system.dto.response.RestaurantResponseDto;
import com.phyo.food_management_system.model.Restaurant;

import java.util.List;
import java.util.Optional;

public interface RestaurantService {
    RestaurantResponseDto addRestaurant(Restaurant restaurant);

    RestaurantResponseDto getRestaurantById(String restaurantId);

    List<RestaurantResponseDto> getAllRestaurants();

    RestaurantResponseDto updateRestaurant(String restaurantId, Restaurant updatedRestaurant);

    void deleteRestaurant(String restaurantId);

    RestaurantResponseDto toRestaurantResponseDto(Restaurant restaurant);

}
