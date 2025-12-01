package com.phyo.food_management_system.dto.response;

import lombok.Data;

@Data
public class OrderItemResponseDto {

    private String menuItemId;
    private Integer quantity;
    private Double price;       // price per item at order time
    private Double totalPrice;  // quantity * price
}
