package com.example.project1.service;

import com.example.project1.dto.ForgotPasswordRequest;
import com.example.project1.dto.ResetPasswordRequest;
import com.example.project1.dto.VerifyOtpRequest;

import java.util.Map;

public interface PasswordResetService {

    /** Sends a fresh OTP by e-mail (same answer whether or not the e-mail is registered). */
    String requestOtp(ForgotPasswordRequest request);

    /** Checks the OTP; on success consumes it and returns a short-lived reset token. */
    Map<String, String> verifyOtp(VerifyOtpRequest request);

    /** Sets the new password (BCrypt) when the reset token is valid. */
    String resetPassword(ResetPasswordRequest request);
}
