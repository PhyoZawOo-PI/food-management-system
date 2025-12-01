package com.phyo.food_management_system.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class MenuNotFoundException extends RuntimeException {

    public MenuNotFoundException(String identifier){
        super(identifier);
    }

}
