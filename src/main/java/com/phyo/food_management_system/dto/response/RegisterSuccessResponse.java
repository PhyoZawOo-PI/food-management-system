package com.phyo.food_management_system.dto.response;

import com.phyo.food_management_system.model.Role;
import lombok.Data;

@Data
public class RegisterSuccessResponse {

    private String id;
    private String email;
    private Role role;
}
