package com.phyo.food_management_system.service.impl;

import com.phyo.food_management_system.dto.response.UserResponseDto;
import com.phyo.food_management_system.exception.UserAlreadyExistsException;
import com.phyo.food_management_system.model.User;
import com.phyo.food_management_system.repository.UserRepository;
import com.phyo.food_management_system.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User register(User user) {
        Optional<User> existing = findByEmail(user.getEmail());
        if (existing.isPresent()) {
            throw new UserAlreadyExistsException(
                    "User with email " + user.getEmail() + " already exists!"
            );
        }
        return userRepository.register(user);
    }

    @Override
    public Optional<User> getUserById(String userId) {
        return userRepository.getUserById(userId);
    }

    @Override
    public List<User> getAllUsers() {
       return userRepository.getAllUsers();
    }

    @Override
    public User updateUser(String userId, User updatedUser) {
        Optional<User> existing = findByEmail(updatedUser.getEmail());

        if (existing.isPresent() && !existing.get().getUserId().equals(userId)) {
            // Another user has this email → cannot use
            throw new UserAlreadyExistsException(
                    "User with email " + updatedUser.getEmail() + " already exists!"
            );
        }

        // Either no user exists with this email, or it’s the same user → allow update
        return userRepository.updateUser(userId, updatedUser);
    }

    @Override
    public void deleteUser(String userId) {
        userRepository.deleteUser(userId);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public UserResponseDto toUserResponseDto(User user) {
        return new UserResponseDto(user.getUserId(), user.getUsername(), user.getEmail(), user.getRole());
    }
}
