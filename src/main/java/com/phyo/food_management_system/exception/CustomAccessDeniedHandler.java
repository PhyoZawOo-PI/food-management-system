package com.phyo.food_management_system.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phyo.food_management_system.dto.response.ErrorResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ErrorResponseDto error = new ErrorResponseDto(
                request.getRequestURI(),
                HttpStatus.FORBIDDEN,
                "Access denied. You do not have permission",
                LocalDateTime.now()
        );

        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
}

