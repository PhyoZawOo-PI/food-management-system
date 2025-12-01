package com.phyo.food_management_system.service.impl;

import com.phyo.food_management_system.dto.response.RestaurantResponseDto;
import com.phyo.food_management_system.exception.RestaurantNotFoundException;
import com.phyo.food_management_system.model.Restaurant;
import com.phyo.food_management_system.repository.RestaurantRepository;
import com.phyo.food_management_system.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository restaurantRepository;

    @Override
    public RestaurantResponseDto addRestaurant(Restaurant restaurant) {
        restaurantRepository.addRestaurant(restaurant);
        return toRestaurantResponseDto(restaurant);
    }

    @Override
    @Cacheable(value = "restaurants", key = "#restaurantId")
    public RestaurantResponseDto getRestaurantById(String restaurantId) {
        log.info("Fetching restaurant from database (not cache)");
        Restaurant restaurant = restaurantRepository.getRestaurantById(restaurantId).orElseThrow(() ->new RestaurantNotFoundException(restaurantId));
        return toRestaurantResponseDto(restaurant);

    }

    @Override
    @Cacheable(value = "restaurants")
    public List<RestaurantResponseDto> getAllRestaurants() {
        log.info("Fetching restaurants from database (not cache)");

        List<Restaurant> restaurants = restaurantRepository.getAllRestaurants();

        return restaurants.stream()
                .map(this::toRestaurantResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public RestaurantResponseDto updateRestaurant(String restaurantId, Restaurant updatedRestaurant) {
        Restaurant restaurant = restaurantRepository.updateRestaurant(restaurantId,updatedRestaurant);
        return toRestaurantResponseDto(restaurant);
    }

    @Override
    public void deleteRestaurant(String restaurantId) {
        restaurantRepository.deleteRestaurant(restaurantId);
    }

    @Override
    public RestaurantResponseDto toRestaurantResponseDto(Restaurant restaurant) {
        return RestaurantResponseDto.builder()
                .id(restaurant.getRestaurantId())
                .name(restaurant.getName())
                .address(restaurant.getAddress())
                .phone(restaurant.getPhone())
                .build();
    }
}
