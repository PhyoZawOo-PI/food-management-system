package com.phyo.food_management_system.controller;

import com.phyo.food_management_system.dto.request.UpdateUserRequest;
import com.phyo.food_management_system.dto.response.ErrorResponseDto;
import com.phyo.food_management_system.dto.response.RegisterResponse;
import com.phyo.food_management_system.dto.response.RegisterSuccessResponse;
import com.phyo.food_management_system.dto.response.UserResponseDto;
import com.phyo.food_management_system.exception.UserNotFoundException;
import com.phyo.food_management_system.model.User;
import com.phyo.food_management_system.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(
        name = "Users",
        description = "APIs for managing user accounts, profiles, and roles"
)

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;



    @Operation(
            summary = "Get all users ((requires JWT))",
            description = "Retrieve a list of all users. Only accessible by users with ADMIN role.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of users retrieved successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = UserResponseDto.class))
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Access denied",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - JWT token missing or invalid",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class)
                            )
                    )
            }
    )
    // Only ADMIN can list all users
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers().stream().map(userService::toUserResponseDto).toList());
    }

    @Operation(
            summary = "Get user by ID (requires JWT)",
            description = "Retrieve user information by ID. A regular user can access only their own data, while an ADMIN can access any user's data.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User data retrieved successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = UserResponseDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Access denied: You do not have permission to access this user",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized: JWT token missing or invalid",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "User not found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class)
                            )
                    )
            }
    )
    // USER can access own info, ADMIN can access anyone
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id.equals(principal.userId)")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable String id, Principal principal) {
        User user = userService.getUserById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        UserResponseDto userResponseDto = userService.toUserResponseDto(user);
        return ResponseEntity.ok(userResponseDto);
    }


    @Operation(
            summary = "Update user information (requires JWT)",
            description = "Update user details. A regular user can update only their own information, while an ADMIN can update any user's information.",
            security = @SecurityRequirement(name = "bearerAuth"),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Updated user details",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UpdateUserRequest.class)

                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User updated successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(example = """
{
  "success": true,
  "data": {
    "id": "string",
    "email": "string",
    "role": "ADMIN"
                   },
  "errors": null
}
""")
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Validation errors occurred",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(example = """
{
  "success": false,
  "data": null,
  "errors": {
    "username": "must not be blank",
    "email": "must be a valid email",
  }
}
""")
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized: JWT token missing or invalid",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class)

                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Access denied: You do not have permission to update this user",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "User not found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class)
                            )
                    )
            },
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "User ID to update",
                            required = true,
                            in = ParameterIn.PATH
                    )
            }
    )
    // USER can update own info, ADMIN can update anyone
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id.equals(principal.userId)")
    public ResponseEntity<RegisterResponse<RegisterSuccessResponse>> updateUser(@PathVariable String id, @Valid @RequestBody UpdateUserRequest updatedUser, BindingResult result, Principal principal) {
        RegisterResponse<RegisterSuccessResponse> registerResponse = new RegisterResponse<>();
        if (result.hasErrors()) {
            Map<String,String> error = new HashMap<>();
            result.getFieldErrors().forEach(err ->
                    error.put(err.getField(),err.getDefaultMessage()));
            registerResponse.setSuccess(false);
            registerResponse.setErrors(error);
            return ResponseEntity.badRequest().body(registerResponse);
        }
        User user = new User();
        user.setUsername(updatedUser.getUsername());
        user.setEmail(updatedUser.getEmail());
        User savedUser = userService.updateUser(id, user);

        RegisterSuccessResponse successResponse = new RegisterSuccessResponse();
        successResponse.setId(savedUser.getUserId());
        successResponse.setEmail(savedUser.getEmail());
        successResponse.setRole(savedUser.getRole());
        registerResponse.setSuccess(true);
        registerResponse.setData(successResponse);
        return ResponseEntity.status(HttpStatus.OK).body(registerResponse);
    }

    @Operation(
            summary = "Delete a user (requires JWT)",
            description = "Deletes a user by ID. Only users with the ADMIN role can perform this action.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User deleted successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = String.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized: JWT token missing or invalid",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden: Only ADMIN can delete users",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "User not found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class)
                            )
                    )
            },
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "ID of the user to delete",
                            required = true,
                            in = ParameterIn.PATH
                    )
            }
    )
    // Only ADMIN can delete users
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteUser(@PathVariable String id) {
        userService.getUserById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        userService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully");
    }
}
