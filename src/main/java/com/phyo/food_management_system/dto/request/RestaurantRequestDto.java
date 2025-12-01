package com.phyo.food_management_system.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RestaurantRequestDto {

    @NotBlank(message = "Restaurant name is required.")
    private String name;
    @NotBlank(message = "Address name is required.")
    private String address;
    @NotBlank(message = "Phone number is required.")
    private String phone;
}
