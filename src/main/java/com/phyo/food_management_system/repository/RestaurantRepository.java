package com.phyo.food_management_system.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.phyo.food_management_system.exception.RestaurantNotFoundException;
import com.phyo.food_management_system.model.Restaurant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RestaurantRepository {

    private final DynamoDBMapper dynamoDBMapper;

    public Restaurant addRestaurant(Restaurant restaurant){
        dynamoDBMapper.save(restaurant);
        return restaurant;
    }

    public Optional<Restaurant> getRestaurantById(String restaurantId) {
        return Optional.ofNullable(dynamoDBMapper.load(Restaurant.class, restaurantId));
    }

    public List<Restaurant> getAllRestaurants() {
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        return dynamoDBMapper.scan(Restaurant.class, scanExpression);
    }

    public Restaurant updateRestaurant(String restaurantId, Restaurant updatedRestaurant) {
        Restaurant existingRestaurant = dynamoDBMapper.load(Restaurant.class, restaurantId);
        if (existingRestaurant != null) {

            existingRestaurant.setName(updatedRestaurant.getName());
            existingRestaurant.setAddress(updatedRestaurant.getAddress());
            existingRestaurant.setPhone(updatedRestaurant.getPhone());
            dynamoDBMapper.save(existingRestaurant);
            existingRestaurant.setRestaurantId(restaurantId);
            return existingRestaurant;
        } else {
            throw new RestaurantNotFoundException(updatedRestaurant.getRestaurantId());
        }
    }

    public void deleteRestaurant(String restaurantId) {
        Restaurant restaurant = dynamoDBMapper.load(Restaurant.class, restaurantId);
        if (restaurant != null) {
            dynamoDBMapper.delete(restaurant);
        } else {
            throw new RestaurantNotFoundException(restaurantId);
        }
    }
}
