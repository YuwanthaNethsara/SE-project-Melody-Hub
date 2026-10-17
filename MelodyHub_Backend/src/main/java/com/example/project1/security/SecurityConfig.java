package com.example.project1.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

/**
 * Central place for ALL authorization rules.
 *
 * Rules are evaluated top to bottom and the FIRST match wins, so the more
 * specific rules must stay above the general ones.
 *
 * Roles (from {@link com.example.project1.enums.Role}):
 * CUSTOMER, ADMIN, INVENTORY_MANAGER, ARTIST, MARKETING_MANAGER,
 * SALES_MANAGER, SUPPLIER
 */
@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // JWT in the Authorization header => no cookies => CSRF not applicable
                .csrf(csrf -> csrf.disable())

                // CORS rules live in CorsConfig
                .cors(cors -> { })

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // the API is never meant to be shown inside a frame
                .headers(headers -> headers.frameOptions(frame -> frame.deny()))

                // 401 = not logged in / bad token, 403 = logged in but not allowed
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) ->
                                writeJson(response, HttpServletResponse.SC_UNAUTHORIZED,
                                        request.getHeader("Authorization") != null
                                                ? "Your session has expired. Please login again."
                                                : "Please log in to continue."))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                writeJson(response, HttpServletResponse.SC_FORBIDDEN,
                                        "You do not have permission to perform this action.")))

                .authorizeHttpRequests(auth -> auth

                        // ---------- infrastructure ----------
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/error").permitAll()

                        // ---------- uploaded images (public) ----------
                        // only image folders are public; MP3s are never served from /uploads
                        .requestMatchers(HttpMethod.GET, "/uploads/products/**", "/uploads/advertisements/**",
                                "/uploads/artists/**", "/uploads/albums/**").permitAll()
                        .requestMatchers("/uploads/**").denyAll()

                        // ---------- public authentication ----------
                        .requestMatchers("/api/auth/register", "/api/auth/login",
                                "/api/auth/forgot-password", "/api/auth/verify-otp",
                                "/api/auth/reset-password").permitAll()

                        // ---------- own profile (any logged-in user) ----------
                        .requestMatchers("/api/auth/me").authenticated()

                        // ---------- follow artists + own notifications (any logged-in user) ----------
                        .requestMatchers("/api/notifications/**").authenticated()

                        // ---------- user management + admin dashboard (ADMIN only) ----------
                        .requestMatchers("/api/auth/create-user",
                                "/api/auth/users", "/api/auth/users/**",
                                "/api/admin/**").hasRole("ADMIN")

                        // ---------- public storefront (read only) ----------
                        .requestMatchers(HttpMethod.GET,
                                "/api/products", "/api/products/**",
                                "/api/categories", "/api/categories/**",
                                "/api/artists", "/api/artists/**",
                                "/api/albums", "/api/albums/**",
                                "/api/songs", "/api/songs/**",
                                "/api/promotions/active",
                                "/api/promotions/product/**",
                                "/api/promotions/search",
                                "/api/featured-products/active",
                                "/api/campaigns/active",
                                "/api/advertisements/active",
                                "/api/reviews/product/**",
                                "/api/reviews/summary",
                                "/api/warranties/product/**").permitAll()

                        // ---------- customer feedback / reviews ----------
                        .requestMatchers(HttpMethod.GET, "/api/reviews/my", "/api/reviews/eligible").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.POST, "/api/reviews").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.PUT, "/api/reviews/*/status").hasAnyRole("ADMIN", "MARKETING_MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/reviews/*").hasRole("CUSTOMER")
                        // customers delete their own review (checked in the service), staff moderate any
                        .requestMatchers(HttpMethod.DELETE, "/api/reviews/*").hasAnyRole("CUSTOMER", "ADMIN", "MARKETING_MANAGER")
                        .requestMatchers("/api/reviews", "/api/reviews/**").hasAnyRole("ADMIN", "MARKETING_MANAGER")

                        // ---------- catalog management ----------
                        .requestMatchers("/api/products", "/api/products/**",
                                "/api/categories", "/api/categories/**",
                                "/api/physical-products", "/api/physical-products/**")
                        .hasAnyRole("ADMIN", "INVENTORY_MANAGER")

                        .requestMatchers("/api/artists", "/api/artists/**", "/api/albums", "/api/albums/**",
                                "/api/songs", "/api/songs/**")
                        .hasAnyRole("ADMIN", "ARTIST")

                        // digital music: any logged-in user can browse, only ADMIN/ARTIST manage
                        .requestMatchers(HttpMethod.GET, "/api/digital-music", "/api/digital-music/**")
                        .authenticated()
                        .requestMatchers("/api/digital-music", "/api/digital-music/**").hasAnyRole("ADMIN", "ARTIST")

                        // ---------- inventory ----------
                        .requestMatchers("/api/inventory", "/api/inventory/**")
                        .hasAnyRole("ADMIN", "INVENTORY_MANAGER")

                        // ---------- marketing ----------
                        .requestMatchers("/api/promotions", "/api/promotions/**",
                                "/api/campaigns", "/api/campaigns/**",
                                "/api/advertisements", "/api/advertisements/**",
                                "/api/featured-products", "/api/featured-products/**")
                        .hasAnyRole("ADMIN", "MARKETING_MANAGER")

                        // ---------- cart (own cart only - enforced in service) ----------
                        .requestMatchers("/api/cart", "/api/cart/**")
                        .hasAnyRole("CUSTOMER", "ADMIN")

                        // ---------- orders / order items / payments ----------
                        // Ownership ("customers only see their own") is enforced in the controllers.
                        .requestMatchers("/api/orders", "/api/orders/**")
                        .hasAnyRole("CUSTOMER", "ADMIN", "SALES_MANAGER")

                        .requestMatchers(HttpMethod.GET, "/api/order-items", "/api/order-items/**")
                        .hasAnyRole("CUSTOMER", "ADMIN", "SALES_MANAGER")
                        .requestMatchers("/api/order-items", "/api/order-items/**").hasRole("ADMIN")

                        .requestMatchers("/api/payments", "/api/payments/**")
                        .hasAnyRole("CUSTOMER", "ADMIN", "SALES_MANAGER")

                        // ---------- warranties ----------
                        .requestMatchers(HttpMethod.GET, "/api/warranties", "/api/warranties/**")
                        .hasAnyRole("ADMIN", "SALES_MANAGER")
                        .requestMatchers("/api/warranties", "/api/warranties/**").hasRole("ADMIN")

                        // ---------- warranty claims ----------
                        .requestMatchers(HttpMethod.POST, "/api/warranty-claims").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.GET,
                                "/api/warranty-claims/my",
                                "/api/warranty-claims/eligible").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.PUT, "/api/warranty-claims/*/status")
                        .hasAnyRole("ADMIN", "SALES_MANAGER")
                        // customers edit / withdraw only their own PENDING claims (checked in the service)
                        .requestMatchers(HttpMethod.PUT, "/api/warranty-claims/*").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.DELETE, "/api/warranty-claims/my/*").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.DELETE, "/api/warranty-claims/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/warranty-claims/status/**")
                        .hasAnyRole("ADMIN", "SALES_MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/warranty-claims")
                        .hasAnyRole("ADMIN", "SALES_MANAGER")
                        // single claim / claims of a user: ownership checked in the controller
                        .requestMatchers(HttpMethod.GET, "/api/warranty-claims/**")
                        .hasAnyRole("CUSTOMER", "ADMIN", "SALES_MANAGER")

                        // ---------- procurement (ADMIN only) ----------
                        .requestMatchers("/api/suppliers", "/api/suppliers/**",
                                "/api/supplier-contracts", "/api/supplier-contracts/**",
                                "/api/purchase-orders", "/api/purchase-orders/**",
                                "/api/supplier-deliveries", "/api/supplier-deliveries/**")
                        .hasRole("ADMIN")

                        // ---------- everything else needs a valid login ----------
                        .anyRequest().authenticated()
                )

                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private static void writeJson(HttpServletResponse response, int status, String message)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"status\":" + status + ",\"message\":\"" + message + "\"}");
    }
}
