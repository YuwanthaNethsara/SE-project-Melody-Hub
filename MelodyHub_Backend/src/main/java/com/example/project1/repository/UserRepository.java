package com.example.project1.repository;

import com.example.project1.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Check whether email already exists
    boolean existsByEmail(String email);

    // Find user by email
    Optional<User> findByEmail(String email);

    // Used to bootstrap the first admin on an empty database
    // Staff who are told about new music content (Observer pattern)
    java.util.List<User> findByRoleIn(java.util.Collection<com.example.project1.enums.Role> roles);

    boolean existsByRole(com.example.project1.enums.Role role);
}