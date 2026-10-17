package com.example.project1.controller;

import com.example.project1.dto.CreateUserRequest;
import com.example.project1.dto.ForgotPasswordRequest;
import com.example.project1.dto.ResetPasswordRequest;
import com.example.project1.dto.VerifyOtpRequest;
import com.example.project1.service.PasswordResetService;
import com.example.project1.dto.LoginRequest;
import com.example.project1.dto.LoginResponse;
import com.example.project1.dto.RegisterRequest;
import com.example.project1.dto.UpdateUserRequest;
import com.example.project1.dto.UserResponse;
import com.example.project1.service.AuthService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    public AuthController(AuthService authService, PasswordResetService passwordResetService) {
        this.authService = authService;
        this.passwordResetService = passwordResetService;
    }

    // PUBLIC - always creates a CUSTOMER
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(
            @Valid @RequestBody RegisterRequest request) {

        String message = authService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", message));
    }

    // PUBLIC - bad credentials => 401 (handled in service)
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {

        return ResponseEntity.ok(authService.login(request));
    }

    // PUBLIC - forgot password, step 1: e-mail a one-time code
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        return ResponseEntity.ok(Map.of("message", passwordResetService.requestOtp(request)));
    }

    // PUBLIC - step 2: check the code, returns a one-time reset token
    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, String>> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request) {

        return ResponseEntity.ok(passwordResetService.verifyOtp(request));
    }

    // PUBLIC - step 3: set the new password (needs the reset token from step 2)
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        return ResponseEntity.ok(Map.of("message", passwordResetService.resetPassword(request)));
    }

    // ADMIN - create a staff account with any role
    @PostMapping("/create-user")
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody CreateUserRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.createUser(request));
    }

    // ANY LOGGED-IN USER - own profile
    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(Authentication authentication) {

        UserResponse user = authService.getUserByEmail(authentication.getName());

        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User no longer exists");
        }

        return ResponseEntity.ok(user);
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateMe(
            Authentication authentication,
            @RequestBody UpdateUserRequest request) {

        return ResponseEntity.ok(
                authService.updateOwnProfile(authentication.getName(), request));
    }

    // ---------- ADMIN ONLY below (enforced in SecurityConfig) ----------

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {

        return ResponseEntity.ok(authService.getAllUsers());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {

        UserResponse user = authService.getUserById(id);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(user);
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<Map<String, String>> updateUser(
            @PathVariable Long id,
            @RequestBody UpdateUserRequest request,
            Authentication authentication) {

        UserResponse target = authService.getUserById(id);

        // An admin must not lock themselves out by changing their own role
        if (target != null
                && target.getEmail().equalsIgnoreCase(authentication.getName())
                && request.getRole() != null
                && request.getRole() != target.getRole()) {

            throw new IllegalArgumentException("You cannot change your own role");
        }

        return ResponseEntity.ok(
                Map.of("message", authService.updateUser(id, request)));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(
            @PathVariable Long id,
            Authentication authentication) {

        UserResponse target = authService.getUserById(id);

        if (target != null && target.getEmail().equalsIgnoreCase(authentication.getName())) {
            throw new IllegalArgumentException("You cannot delete your own account");
        }

        return ResponseEntity.ok(
                Map.of("message", authService.deleteUser(id)));
    }
}
