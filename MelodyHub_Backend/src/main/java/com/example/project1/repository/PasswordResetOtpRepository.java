package com.example.project1.repository;

import com.example.project1.entity.PasswordResetOtp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetOtpRepository extends JpaRepository<PasswordResetOtp, Long> {

    Optional<PasswordResetOtp> findFirstByEmailIgnoreCaseOrderByCreatedAtDesc(String email);

    void deleteByEmailIgnoreCase(String email);
}
