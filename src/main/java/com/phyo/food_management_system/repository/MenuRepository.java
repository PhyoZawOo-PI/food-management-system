package com.phyo.food_management_system.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.phyo.food_management_system.exception.MenuNotFoundException;
import com.phyo.food_management_system.model.Menu;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MenuRepository {

    private final DynamoDBMapper dynamoDBMapper;

    // Add new menu item
    public Menu addMenuItem(Menu menu) {
        dynamoDBMapper.save(menu);
        return menu;
    }

    // Get menu item by menuId
    public Optional<Menu> getMenuItemById(String menuId) {
        return Optional.ofNullable(dynamoDBMapper.load(Menu.class, menuId));
    }

    // List all menu items for a restaurant (Using GSI: restaurantId_index)
    public List<Menu> getMenuByRestaurantId(String restaurantId) {
        DynamoDBQueryExpression<Menu> queryExpression = new DynamoDBQueryExpression<Menu>()
                .withIndexName("restaurantId_index")
                .withConsistentRead(false)
                .withKeyConditionExpression("restaurantId = :resId")
                .withExpressionAttributeValues(
                        Map.of(":resId", new AttributeValue().withS(restaurantId))
                );

        return dynamoDBMapper.query(Menu.class, queryExpression);
    }

    // Update menu item
    public Menu updateMenuItem(String menuId, Menu updatedMenu) {
        Menu existingMenu = dynamoDBMapper.load(Menu.class, menuId);

        if (existingMenu != null) {
            existingMenu.setName(updatedMenu.getName());
            existingMenu.setPrice(updatedMenu.getPrice());
            existingMenu.setRestaurantId(updatedMenu.getRestaurantId());
            existingMenu.setDescription(updatedMenu.getDescription());
            dynamoDBMapper.save(existingMenu);
            return existingMenu;
        } else {
            throw new MenuNotFoundException(menuId);
        }
    }

    // Delete menu item
    public void deleteMenuItem(String menuId) {
        Menu menu = dynamoDBMapper.load(Menu.class, menuId);
        if (menu != null) {
            dynamoDBMapper.delete(menu);
        } else {
            throw new MenuNotFoundException(menuId);
        }
    }
}

