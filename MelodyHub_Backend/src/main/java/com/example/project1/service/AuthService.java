package com.example.project1.service;

import com.example.project1.dto.CreateUserRequest;
import com.example.project1.dto.LoginRequest;
import com.example.project1.dto.LoginResponse;
import com.example.project1.dto.RegisterRequest;
import com.example.project1.dto.UpdateUserRequest;
import com.example.project1.dto.UserResponse;

import java.util.List;

public interface AuthService {

    /** Public registration - always creates a CUSTOMER. */
    String register(RegisterRequest request);

    /** ADMIN creates a user with any role. */
    UserResponse createUser(CreateUserRequest request);

    LoginResponse login(LoginRequest request);

    List<UserResponse> getAllUsers();

    UserResponse getUserById(Long id);

    UserResponse getUserByEmail(String email);

    String updateUser(Long id, UpdateUserRequest request);

    /** The logged-in user edits their own name / password. */
    UserResponse updateOwnProfile(String email, UpdateUserRequest request);

    String deleteUser(Long id);
}
