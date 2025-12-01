package com.phyo.food_management_system.dto.response;

import com.phyo.food_management_system.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserResponseDto {

    private String id;
    private String name;
    private String email;
    private Role role;

}
