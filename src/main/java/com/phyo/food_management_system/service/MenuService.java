package com.phyo.food_management_system.service;

import com.phyo.food_management_system.dto.request.MenuRequestDto;
import com.phyo.food_management_system.dto.response.MenuResponseDto;
import com.phyo.food_management_system.model.Menu;

import java.util.List;
import java.util.Optional;

public interface MenuService {

    // Add Menu Item (ADMIN only)
    MenuResponseDto addMenuItem(MenuRequestDto menuRequestDto);

    // List Menu Items by Restaurant (USER/ADMIN)
    List<MenuResponseDto> getMenuItemsByRestaurantId(String restaurantId);

    // Get Menu by Menu ID (ADMIN only)
    MenuResponseDto getMenuByMenuId(String menuId);

    // Update Menu Item (ADMIN only)
    MenuResponseDto updateMenuItem(String menuId, MenuRequestDto menuRequestDto);

    // Remove Menu Item (ADMIN only)
    void deleteMenuItem(String menuId);

    //Map to MenuResponseDto
    MenuResponseDto toMenuResponseDto(Menu menu);
}

