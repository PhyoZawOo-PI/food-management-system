package com.phyo.food_management_system.model;

import lombok.Data;

@Data
public class OrderItem {

    private String menuItemId;
    private Integer quantity;
    private Double price;       // price per item at time of purchase
    private Double totalPrice;  // quantity * price
}

