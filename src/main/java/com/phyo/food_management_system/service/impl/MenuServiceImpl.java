package com.phyo.food_management_system.service.impl;

import com.phyo.food_management_system.dto.request.MenuRequestDto;
import com.phyo.food_management_system.dto.response.MenuResponseDto;
import com.phyo.food_management_system.exception.MenuNotFoundException;
import com.phyo.food_management_system.exception.RestaurantNotFoundException;
import com.phyo.food_management_system.model.Menu;
import com.phyo.food_management_system.repository.MenuRepository;
import com.phyo.food_management_system.repository.RestaurantRepository;
import com.phyo.food_management_system.service.MenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuServiceImpl implements MenuService {

    private final MenuRepository menuRepository;
    private final RestaurantRepository restaurantRepository; // To verify restaurant exists

    @Override
    public MenuResponseDto addMenuItem(MenuRequestDto dto) {

        // Check if restaurant exists
        restaurantRepository.getRestaurantById(dto.getRestaurantId())
                .orElseThrow(() -> new RestaurantNotFoundException(dto.getRestaurantId()));

        Menu menu = new Menu();
        menu.setRestaurantId(dto.getRestaurantId());
        menu.setName(dto.getName());
        menu.setDescription(dto.getDescription());
        menu.setPrice(dto.getPrice());

        Menu savedMenu = menuRepository.addMenuItem(menu);
        return toMenuResponseDto(savedMenu);
    }

    @Override
    @Cacheable(value = "menuItems", key = "#restaurantId")
    public List<MenuResponseDto> getMenuItemsByRestaurantId(String restaurantId) {
        log.info("Fetching menus from database (not cache)");
        // Optional: check restaurant exists (recommended)
        restaurantRepository.getRestaurantById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));

        List<Menu> menuList = menuRepository.getMenuByRestaurantId(restaurantId);
        return menuList.stream().map(this::toMenuResponseDto).collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "menuItems", key = "#menuId")
    public MenuResponseDto getMenuByMenuId(String menuId) {
        log.info("Fetching menu from database (not cache)");
        Menu menu = menuRepository.getMenuItemById(menuId).orElseThrow(() -> new MenuNotFoundException(menuId));
        return toMenuResponseDto(menu);
    }

    @Override
    public MenuResponseDto updateMenuItem(String menuId, MenuRequestDto dto) {

        Menu existingMenu = menuRepository.getMenuItemById(menuId)
                .orElseThrow(() -> new MenuNotFoundException(menuId));

        existingMenu.setName(dto.getName());
        existingMenu.setDescription(dto.getDescription());
        existingMenu.setPrice(dto.getPrice());
        existingMenu.setRestaurantId(dto.getRestaurantId());

        Menu menu = menuRepository.updateMenuItem(menuId,existingMenu);
        return toMenuResponseDto(menu);
    }

    @Override
    public void deleteMenuItem(String menuId) {
        menuRepository.deleteMenuItem(menuId);
    }

    @Override
    public MenuResponseDto toMenuResponseDto(Menu menu) {

        return new MenuResponseDto(
                menu.getMenuId(),
                menu.getRestaurantId(),
                menu.getName(),
                menu.getDescription(),
                menu.getPrice()
        );
    }
}

