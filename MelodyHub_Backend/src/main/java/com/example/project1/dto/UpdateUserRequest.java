package com.example.project1.dto;

import com.example.project1.enums.Role;

/**
 * Used by:
 *  - ADMIN  (PUT /api/auth/users/{id})  : name, email, role, optional new password
 *  - the logged-in user (PUT /api/auth/me): name and password only
 *    (changing the password then requires currentPassword).
 */
public class UpdateUserRequest {

    private String name;
    private String email;
    private String password;
    private String currentPassword;
    private Role role;

    public UpdateUserRequest() {
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getCurrentPassword() { return currentPassword; }
    public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}
