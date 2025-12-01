package com.phyo.food_management_system.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final DynamoUserDetailsService userDetailsService;


    public JwtFilter(JwtUtil jwtUtil, DynamoUserDetailsService uds) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = uds;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // 1. Get the path after stripping the context-path (/api/v1)
        String path = request.getRequestURI().substring(request.getContextPath().length());

        // 2. Normalize the path (remove trailing slash if present)
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        // 3. Define the excluded path CONSISTENT with the result of the normalization!
        // The normalized path will be "/auth/login"
        final String LOGIN_PATH = "/auth/login"; // <--- CORRECTED TO MATCH NORMALIZED PATH

        // 4. Use a normalized comparison
        if (LOGIN_PATH.equals(path)) {
            chain.doFilter(request, response);  // Skip JWT validation
            return;
        }
        SecurityContextHolder.clearContext();
        String authHeader = request.getHeader("Authorization");
        String jwt = null;
        String username = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);

            try {
                username = jwtUtil.extractUsername(jwt); // only parse inside try-block
            } catch (Exception e) {
                // Invalid token — do NOT throw exception, just skip authentication
                SecurityContextHolder.clearContext();
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails details = userDetailsService.loadUserByUsername(username);

            if (jwtUtil.validateToken(jwt, details)) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        chain.doFilter(request, response);
    }
}
