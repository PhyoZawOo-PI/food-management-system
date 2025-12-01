package com.phyo.food_management_system.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class RegisterResponse<T> {

   private boolean success;
   private T data;
   private Map<String,String> errors;
}
