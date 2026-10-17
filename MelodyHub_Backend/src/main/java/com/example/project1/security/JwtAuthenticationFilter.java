package com.example.project1.security;

import com.example.project1.entity.User;
import com.example.project1.repository.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserRepository userRepository
    ) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader =
                request.getHeader("Authorization");

        // No Authorization header
        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        String token =
                authHeader.substring(7);

        try {

            // Check JWT
            if (!jwtService.isTokenValid(token)) {

                SecurityContextHolder.clearContext();

                filterChain.doFilter(request, response);
                return;
            }

            // Get email from JWT
            String email =
                    jwtService.extractEmail(token);

            // Get user from DATABASE
            User user = userRepository
                    .findByEmail(email)
                    .orElse(null);

            if (user == null || user.getRole() == null) {

                SecurityContextHolder.clearContext();

                filterChain.doFilter(request, response);
                return;
            }

            // Get role directly from DB
            String role =
                    user.getRole().name();

            // Example:
            // CUSTOMER -> ROLE_CUSTOMER
            // ADMIN -> ROLE_ADMIN

            SimpleGrantedAuthority authority =
                    new SimpleGrantedAuthority(
                            "ROLE_" + role
                    );

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            email,
                            null,
                            List.of(authority)
                    );

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(authentication);

        } catch (Exception e) {

            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}