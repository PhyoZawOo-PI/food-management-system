package com.phyo.food_management_system.service;

import com.phyo.food_management_system.dto.response.UserResponseDto;
import com.phyo.food_management_system.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    User register(User user);

    Optional<User> findByEmail(String email);

    Optional<User> getUserById(String userId);

    List<User> getAllUsers();

    User updateUser(String userId, User updatedUser);

    void deleteUser(String userId);

    UserResponseDto toUserResponseDto(User user);
}
