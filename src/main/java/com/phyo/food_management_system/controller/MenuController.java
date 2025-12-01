package com.phyo.food_management_system.controller;

import com.phyo.food_management_system.dto.request.MenuRequestDto;
import com.phyo.food_management_system.dto.response.ErrorResponseDto;
import com.phyo.food_management_system.dto.response.MenuResponseDto;
import com.phyo.food_management_system.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Menus",
        description = "APIs for managing restaurant menu items"
)
@RestController
@RequestMapping("/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;


    @Operation(
            summary = "Add a new menu item",
            description = "Allows only ADMIN users to add a new menu item. Requires a valid JWT token.",
            security = {
                    @SecurityRequirement(name = "bearerAuth")
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Details of the menu item to create",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MenuRequestDto.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Menu item created successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = MenuResponseDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Bad Request – Invalid menu item data",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized – Missing or invalid JWT token",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden – Only ADMIN users can add menu items",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class)
                            )
                    )
            }
    )
    //ADD MENU ITEM (ADMIN ONLY)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MenuResponseDto> addMenuItem(@Valid @RequestBody MenuRequestDto dto) {

        MenuResponseDto menu = menuService.addMenuItem(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(menu);
    }

    @Operation(
            summary = "Get menu items by restaurant ID",
            description = "Allows USER or ADMIN to retrieve all menu items belonging to a specific restaurant. Requires a valid JWT token.",
            security = {
                    @SecurityRequirement(name = "bearerAuth")
            },
            parameters = {
                    @Parameter(
                            name = "restaurantId",
                            description = "ID of the restaurant whose menu items should be retrieved",
                            required = true,
                            in = ParameterIn.PATH
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Menu items retrieved successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = MenuResponseDto.class))
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized – Missing or invalid JWT token",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden – Only USER or ADMIN roles can access this endpoint",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Restaurant or Menu not found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class)
                            )
                    )
            }
    )
    //LIST MENU ITEMS BY RESTAURANT (USER / ADMIN)
    @GetMapping("/restaurant/{restaurantId}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<List<MenuResponseDto>> getMenuByRestaurant(
            @PathVariable String restaurantId) {

        List<MenuResponseDto> menuItems = menuService.getMenuItemsByRestaurantId(restaurantId);
        return ResponseEntity.ok(menuItems);
    }

    @Operation(
            summary = "Get menu item by menu ID",
            description = "Allows ADMIN to retrieve a specific menu item by its ID. Requires a valid JWT token.",
            security = {
                    @SecurityRequirement(name = "bearerAuth")
            },
            parameters = {
                    @Parameter(
                            name = "menuId",
                            description = "ID of the menu item to retrieve",
                            required = true,
                            in = ParameterIn.PATH
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Menu item retrieved successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = MenuResponseDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized – Missing or invalid JWT token",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden – Only ADMIN users can access this endpoint",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Menu item not found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class)
                            )
                    )
            }
    )
    // GET MENU ITEM BY MENU ID (ADMIN)
    @GetMapping("/{menuId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MenuResponseDto> getMenuByMenuId(
            @PathVariable String menuId) {

        MenuResponseDto menu = menuService.getMenuByMenuId(menuId);

        return ResponseEntity.ok(menu);
    }

    @Operation(
            summary = "Update menu item",
            description = "Allows only ADMIN users to update menu item. Requires a valid JWT token.",
            security = {
                    @SecurityRequirement(name = "bearerAuth")
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Details of the menu item to update",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MenuRequestDto.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Menu item updated successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = MenuResponseDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Bad Request – Invalid menu item data",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized – Missing or invalid JWT token",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden – Only ADMIN users can add menu items",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class)
                            )
                    )
            }
    )
    @PutMapping("/{menuId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MenuResponseDto> updateMenuByMenuId(
            @PathVariable String menuId, @RequestBody MenuRequestDto menuRequestDto) {

        MenuResponseDto menu = menuService.updateMenuItem(menuId, menuRequestDto);

        return ResponseEntity.ok(menu);

    }


    @Operation(
            summary = "Delete menu item by ID",
            description = "Allows ADMIN to delete a specific menu item by its ID. Requires a valid JWT token.",
            security = {
                    @SecurityRequirement(name = "bearerAuth")
            },
            parameters = {
                    @Parameter(
                            name = "menuId",
                            description = "ID of the menu item to delete",
                            required = true,
                            in = ParameterIn.PATH
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Menu item deleted successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = String.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized – Missing or invalid JWT token",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden – Only ADMIN users can delete menu items",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Menu item not found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class)
                            )
                    )
            }
    )
    // DELETE MENU ITEM (ADMIN)
    @DeleteMapping("/{menuId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteMenuItem(@PathVariable String menuId) {

        menuService.getMenuByMenuId(menuId);

        menuService.deleteMenuItem(menuId);
        return ResponseEntity.ok("Menu deleted successfully!");
    }
}

