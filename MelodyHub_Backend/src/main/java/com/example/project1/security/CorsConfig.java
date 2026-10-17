package com.example.project1.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Single source of truth for CORS (the per-controller @CrossOrigin
 * annotations were removed so nothing can silently widen this).
 *
 * Allowed origins are local development servers only by default
 * (VS Code Live Server = 5500, plus common dev ports). For a real
 * deployment set melodyhub.cors.allowed-origins to the exact frontend URL.
 */
@Configuration
public class CorsConfig {

    @Value("${melodyhub.cors.allowed-origins:http://localhost:*,http://127.0.0.1:*}")
    private String allowedOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        // patterns allow "http://localhost:*" (any dev port) as well as exact origins
        configuration.setAllowedOriginPatterns(origins);

        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));

        // lets the browser read the file name of MP3 downloads
        configuration.setExposedHeaders(List.of("Content-Disposition"));

        // the JWT travels in the Authorization header, never in cookies
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
