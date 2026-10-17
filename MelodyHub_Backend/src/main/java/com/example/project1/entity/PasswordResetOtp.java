package com.example.project1.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * One row per e-mail address that is currently resetting its password.
 * Only BCrypt hashes are stored - never the OTP or the reset token itself.
 * A new request for the same e-mail replaces the old row, so an older OTP
 * stops working immediately. New table; no existing table is touched.
 */
@Entity
@Table(name = "password_reset_otps",
        indexes = @Index(name = "idx_pwreset_email", columnList = "email"))
public class PasswordResetOtp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String otpHash;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private int attempts;

    /** true once the OTP has been checked successfully (OTP is then single-use / consumed) */
    @Column(nullable = false)
    private boolean otpUsed;

    /** hash of the short-lived token handed out after a correct OTP */
    private String resetTokenHash;

    private LocalDateTime resetExpiresAt;

    public PasswordResetOtp() {
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getOtpHash() { return otpHash; }
    public void setOtpHash(String otpHash) { this.otpHash = otpHash; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public int getAttempts() { return attempts; }
    public void setAttempts(int attempts) { this.attempts = attempts; }
    public boolean isOtpUsed() { return otpUsed; }
    public void setOtpUsed(boolean otpUsed) { this.otpUsed = otpUsed; }
    public String getResetTokenHash() { return resetTokenHash; }
    public void setResetTokenHash(String resetTokenHash) { this.resetTokenHash = resetTokenHash; }
    public LocalDateTime getResetExpiresAt() { return resetExpiresAt; }
    public void setResetExpiresAt(LocalDateTime resetExpiresAt) { this.resetExpiresAt = resetExpiresAt; }
}
