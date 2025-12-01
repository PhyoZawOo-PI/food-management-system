package com.phyo.food_management_system.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuResponseDto {

    private String menuId;
    private String restaurantId;   // IMPORTANT
    private String name;
    private String description;
    private Double price;
}
