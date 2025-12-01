package com.phyo.food_management_system.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phyo.food_management_system.dto.response.ErrorResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ErrorResponseDto error = new ErrorResponseDto(
                request.getRequestURI(),
                HttpStatus.UNAUTHORIZED,
                "Invalid or missing JWT token",
                LocalDateTime.now()
        );

        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
}

