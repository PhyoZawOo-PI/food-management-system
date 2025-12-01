package com.phyo.food_management_system.controller;

import com.phyo.food_management_system.dto.request.LoginRequest;
import com.phyo.food_management_system.dto.request.RegisterRequest;
import com.phyo.food_management_system.dto.response.LoginResponse;
import com.phyo.food_management_system.dto.response.RegisterResponse;
import com.phyo.food_management_system.dto.response.RegisterSuccessResponse;
import com.phyo.food_management_system.model.User;
import com.phyo.food_management_system.security.JwtUtil;
import com.phyo.food_management_system.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
@Tag(
        name = "User Login & Registration",
        description = "REST APIs for handling user login and registration processes"
)
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder encoder;
    private final UserService userService;


    @Operation(
            summary = "Register a new user (requires JWT)",
            description = "Creates a new user account. The request must include a valid JWT token in the Authorization header.",
            security = @SecurityRequirement(name = "bearerAuth"),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "User registration request",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RegisterRequest.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Registration successful",
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
                            description = "Validation failed",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(example = """
{
  "success": false,
  "data": null,
  "errors": {
    "username": "must not be blank",
    "email": "must be a valid email",
    "password": "must be at least 8 characters"
  }
}
""")
                            )
                    )
            }
    )

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse<RegisterSuccessResponse>> register(@Valid @RequestBody RegisterRequest req, BindingResult result) {
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
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setPassword(encoder.encode(req.getPassword()));
        user.setRole(req.getRole());
        User savedUser = userService.register(user);

        RegisterSuccessResponse successResponse = new RegisterSuccessResponse();
        successResponse.setId(savedUser.getUserId());
        successResponse.setEmail(savedUser.getEmail());
        successResponse.setRole(savedUser.getRole());
        registerResponse.setSuccess(true);
        registerResponse.setData(successResponse);
        return ResponseEntity.status(HttpStatus.CREATED).body(registerResponse);
    }


    @Operation(
            summary = "User login",
            description = "Authenticates a user with email and password and returns a JWT token with expiration time.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "User login credentials",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginRequest.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User successfully authenticated",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = LoginResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Invalid email or password",
                            content = @Content(
                                    mediaType = "application/json"
                            )
                    ),
            }
    )
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtUtil.generateToken(userDetails);
        long expiresIn = jwtUtil.getExpiration()/ (1000 * 60);

        return ResponseEntity.status(HttpStatus.OK).body(new LoginResponse(token, expiresIn));
    }
}
