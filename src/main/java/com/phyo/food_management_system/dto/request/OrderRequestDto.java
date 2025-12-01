package com.phyo.food_management_system.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequestDto {

    private String userId;
    @NotBlank(message = "Restaurant id is required.")
    private String restaurantId;
    @NotEmpty(message = "Order items are required.")
    @Valid
    private List<OrderItemRequestDto> items;

    @Data
    public static class OrderItemRequestDto {
        @NotBlank(message = "Menu item id is required.")
        private String menuItemId;
        @NotBlank(message = "Quantity is required.")
        private Integer quantity;
    }
}
