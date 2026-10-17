package com.example.project1.dto;

import com.example.project1.enums.Role;

public class LoginResponse {

    private String message;
    private Long id;
    private String name;
    private String email;
    private Role role;
    private String token;

    public LoginResponse() {
    }

    public LoginResponse(
            String message,
            Long id,
            String name,
            String email,
            Role role,
            String token) {

        this.message = message;
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.token = token;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}