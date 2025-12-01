package com.phyo.food_management_system.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MenuRequestDto {

    @NotBlank(message = "Restaurant id is required.")
    private String restaurantId;
    @NotBlank(message = "Menu name is required.")
    private String name;
    @NotBlank(message = "Menu description is required.")
    private String description;
    @NotNull(message = "Menu price is required.")
    @Positive(message = "Menu price must be greater than 0.")
    private Double price;
}
