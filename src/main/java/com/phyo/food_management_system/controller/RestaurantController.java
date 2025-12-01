package com.phyo.food_management_system.controller;

import com.phyo.food_management_system.dto.request.RestaurantRequestDto;
import com.phyo.food_management_system.dto.response.ErrorResponseDto;
import com.phyo.food_management_system.dto.response.RestaurantResponseDto;
import com.phyo.food_management_system.model.Restaurant;
import com.phyo.food_management_system.service.RestaurantService;
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
        name = "Restaurants",
        description = "APIs for creating, retrieving, updating, and deleting restaurants"
)

@RestController
@RequestMapping("/restaurant")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    @Operation(
            summary = "Add a new restaurant",
            description = "Creates a new restaurant. Requires ADMIN role and a valid JWT token.",
            security = @SecurityRequirement(name = "bearerAuth"),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Restaurant details to create",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RestaurantRequestDto.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Restaurant created successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = RestaurantResponseDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input data",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - JWT token missing or invalid",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - Only ADMIN users can add restaurants",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class)
                            )
                    )
            }
    )
    // Add Restaurant
    @PostMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RestaurantResponseDto> addRestaurant(@Valid @RequestBody RestaurantRequestDto restaurantRequestDto){
        Restaurant restaurant = new Restaurant();
        restaurant.setName(restaurantRequestDto.getName());
        restaurant.setPhone(restaurantRequestDto.getPhone());
        restaurant.setAddress(restaurantRequestDto.getAddress());

        restaurantService.addRestaurant(restaurant);

        return ResponseEntity.status(HttpStatus.CREATED).body(restaurantService.toRestaurantResponseDto(restaurant));

}

    @Operation(
            summary = "Get restaurant details by ID",
            description = "Allows USER or ADMIN to view restaurant details by ID. Requires a valid JWT token.",
            security = {
                    @SecurityRequirement(name = "bearerAuth")
            },
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "Restaurant ID to retrieve",
                            required = true,
                            in = ParameterIn.PATH
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Restaurant details retrieved successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = RestaurantResponseDto.class)
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
                            description = "Forbidden – Only USER or ADMIN can access this resource",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Restaurant not found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class)
                            )
                    )
            }
    )
    //View Restaurant Details by ID - USER/ADMIN
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<RestaurantResponseDto> getRestaurantById(@PathVariable String id) {
        RestaurantResponseDto restaurant = restaurantService.getRestaurantById(id);
        return ResponseEntity.ok(restaurant);
    }

    @Operation(
            summary = "Get list of all restaurants",
            description = "Allows USER or ADMIN to view the list of all restaurants. Requires a valid JWT token.",
            security = {
                    @SecurityRequirement(name = "bearerAuth")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of restaurants retrieved successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(
                                            schema = @Schema(implementation = RestaurantResponseDto.class)
                                    )
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
                            description = "Forbidden – Only USER or ADMIN can access this resource",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class)
                            )
                    )
            }
    )

    //View Restaurant List - USER/ADMIN
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping
    public ResponseEntity<List<RestaurantResponseDto>> getAllRestaurants() {
        List<RestaurantResponseDto> restaurants = restaurantService.getAllRestaurants();
        return ResponseEntity.ok(restaurants);
    }

    @Operation(
            summary = "Update an existing restaurant",
            description = "Allows only ADMIN users to update restaurant information. Requires a valid JWT token.",
            security = {
                    @SecurityRequirement(name = "bearerAuth")
            },
            parameters = {
                    @Parameter(
                            name = "id",
                            in = ParameterIn.PATH,
                            required = true,
                            description = "ID of the restaurant to update"
                    )
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Updated restaurant details",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RestaurantRequestDto.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Restaurant updated successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = RestaurantResponseDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Bad Request – Invalid restaurant input data",
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
                            description = "Forbidden – Only ADMIN users can update restaurants",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Restaurant not found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class)
                            )
                    )
            }
    )

    //Update Restaurant - ADMIN only
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<RestaurantResponseDto> updateRestaurant(
            @PathVariable String id,
            @Valid @RequestBody RestaurantRequestDto restaurantRequestDto) {

        Restaurant restaurant = new Restaurant();

        restaurant.setName(restaurantRequestDto.getName());
        restaurant.setPhone(restaurantRequestDto.getPhone());
        restaurant.setAddress(restaurantRequestDto.getAddress());

        restaurantService.updateRestaurant(id, restaurant);

        RestaurantResponseDto responseDto = restaurantService.toRestaurantResponseDto(restaurant);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(
            summary = "Delete a restaurant",
            description = "Allows only ADMIN users to delete a restaurant by ID. Requires a valid JWT token.",
            security = {
                    @SecurityRequirement(name = "bearerAuth")
            },
            parameters = {
                    @Parameter(
                            name = "id",
                            in = ParameterIn.PATH,
                            required = true,
                            description = "ID of the restaurant to delete"
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Restaurant deleted successfully",
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
                            description = "Forbidden – Only ADMIN users can delete restaurants",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Restaurant not found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class)
                            )
                    )
            }
    )

    //Delete Restaurant - ADMIN only
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteRestaurant(@PathVariable String id) {
        restaurantService.getRestaurantById(id);
        restaurantService.deleteRestaurant(id);
        return ResponseEntity.ok("Restaurant deleted successfully!");
    }
}


