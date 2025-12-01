package com.phyo.food_management_system.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantResponseDto{

    private String id;
    private String name;
    private String address;
    private String phone;
}
