package com.phyo.food_management_system.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter@Setter
public class LoginResponse {

    private String token;
    private long expiresIn;

}
