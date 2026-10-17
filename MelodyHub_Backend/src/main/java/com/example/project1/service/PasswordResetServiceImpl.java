package com.example.project1.service;

import com.example.project1.dto.ForgotPasswordRequest;
import com.example.project1.dto.ResetPasswordRequest;
import com.example.project1.dto.VerifyOtpRequest;
import com.example.project1.entity.PasswordResetOtp;
import com.example.project1.entity.User;
import com.example.project1.repository.PasswordResetOtpRepository;
import com.example.project1.repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;

/**
 * Forgot-password flow with a real e-mailed OTP.
 *
 *  1. requestOtp   -> random 6 digit code, stored only as a BCrypt hash, valid 5 minutes,
 *                     e-mailed to the registered address. A new request replaces the old one.
 *  2. verifyOtp    -> wrong code counts as an attempt (max 5), expired / used codes are rejected.
 *                     A correct code is consumed and swapped for a one-time reset token (10 min).
 *  3. resetPassword-> needs that token; stores the new BCrypt password and deletes the row.
 *
 * The OTP is never logged and never returned in a response.
 */
@Service
public class PasswordResetServiceImpl implements PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetServiceImpl.class);

    private static final int MAX_ATTEMPTS = 5;
    private static final String GENERIC_SENT =
            "If this email is registered, a verification code has been sent to it.";

    private final UserRepository userRepository;
    private final PasswordResetOtpRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final SecureRandom random = new SecureRandom();

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Value("${melodyhub.mail.from:}")
    private String mailFrom;

    @Value("${melodyhub.password-reset.otp-ttl-minutes:5}")
    private long otpTtlMinutes;

    @Value("${melodyhub.password-reset.resend-cooldown-seconds:60}")
    private long cooldownSeconds;

    @Value("${melodyhub.password-reset.reset-token-ttl-minutes:10}")
    private long resetTtlMinutes;

    public PasswordResetServiceImpl(UserRepository userRepository,
                                    PasswordResetOtpRepository otpRepository,
                                    PasswordEncoder passwordEncoder,
                                    ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.userRepository = userRepository;
        this.otpRepository = otpRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSenderProvider = mailSenderProvider;
    }

    // =========================
    // STEP 1 - send OTP
    // =========================
    @Override
    @Transactional
    public String requestOtp(ForgotPasswordRequest request) {

        String email = request.getEmail().trim();
        User user = userRepository.findByEmail(email).orElse(null);

        // Unknown address: answer exactly like a known one (no account enumeration)
        if (user == null) {
            return GENERIC_SENT;
        }

        PasswordResetOtp previous = otpRepository
                .findFirstByEmailIgnoreCaseOrderByCreatedAtDesc(email).orElse(null);

        if (previous != null
                && previous.getCreatedAt().plusSeconds(cooldownSeconds).isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Please wait a moment before requesting another code.");
        }

        JavaMailSender sender = mailSenderProvider.getIfAvailable();
        if (sender == null || mailHost == null || mailHost.isBlank()) {
            log.error("Password reset requested but no mail server is configured (set MAIL_HOST, MAIL_USERNAME, MAIL_PASSWORD).");
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Email service is not available right now. Please try again later.");
        }

        String otp = String.format("%06d", random.nextInt(1_000_000));

        // a new OTP replaces any older one for this e-mail
        otpRepository.deleteByEmailIgnoreCase(email);
        otpRepository.flush();

        PasswordResetOtp row = new PasswordResetOtp();
        row.setEmail(user.getEmail());
        row.setOtpHash(passwordEncoder.encode(otp));
        row.setCreatedAt(LocalDateTime.now());
        row.setExpiresAt(LocalDateTime.now().plusMinutes(otpTtlMinutes));
        row.setAttempts(0);
        row.setOtpUsed(false);
        PasswordResetOtp saved = otpRepository.save(row);

        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            if (mailFrom != null && !mailFrom.isBlank()) {
                mail.setFrom(mailFrom);
            }
            mail.setTo(user.getEmail());
            mail.setSubject("MelodyHub password reset code");
            mail.setText("Hi " + user.getName() + ",\n\n"
                    + "Your MelodyHub verification code is: " + otp + "\n\n"
                    + "It expires in " + otpTtlMinutes + " minutes and can be used once.\n"
                    + "If you did not ask to reset your password, you can ignore this email.\n\n"
                    + "MelodyHub");
            sender.send(mail);
        } catch (Exception ex) {
            // never keep a code that the user could not receive; never log the code
            otpRepository.delete(saved);
            log.error("Could not send password reset email: {}", ex.getClass().getSimpleName());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "We could not send the verification email. Please try again later.");
        }

        return GENERIC_SENT;
    }

    // =========================
    // STEP 2 - verify OTP
    // =========================
    @Override
    @Transactional(noRollbackFor = ResponseStatusException.class)
    public Map<String, String> verifyOtp(VerifyOtpRequest request) {

        PasswordResetOtp row = otpRepository
                .findFirstByEmailIgnoreCaseOrderByCreatedAtDesc(request.getEmail().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Invalid or expired code. Please request a new one."));

        if (row.isOtpUsed()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "This code has already been used. Please request a new one.");
        }

        if (row.getExpiresAt().isBefore(LocalDateTime.now())) {
            otpRepository.delete(row);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "This code has expired. Please request a new one.");
        }

        if (row.getAttempts() >= MAX_ATTEMPTS) {
            otpRepository.delete(row);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Too many incorrect attempts. Please request a new code.");
        }

        if (!passwordEncoder.matches(request.getOtp(), row.getOtpHash())) {
            row.setAttempts(row.getAttempts() + 1);
            otpRepository.save(row);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Incorrect verification code.");
        }

        // correct: consume the OTP, hand out a one-time reset token
        byte[] raw = new byte[32];
        random.nextBytes(raw);
        String resetToken = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);

        row.setOtpUsed(true);
        row.setResetTokenHash(passwordEncoder.encode(resetToken));
        row.setResetExpiresAt(LocalDateTime.now().plusMinutes(resetTtlMinutes));
        otpRepository.save(row);

        return Map.of("message", "Code verified.", "resetToken", resetToken);
    }

    // =========================
    // STEP 3 - reset password
    // =========================
    @Override
    @Transactional
    public String resetPassword(ResetPasswordRequest request) {

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords do not match.");
        }
        String newPassword = request.getNewPassword();
        if (newPassword.length() > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at most 72 characters.");
        }
        if (!newPassword.matches(".*[A-Za-z].*") || !newPassword.matches(".*\\d.*")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Password must contain at least one letter and one number.");
        }

        String email = request.getEmail().trim();
        String invalid = "This reset session is invalid or has expired. Please start again.";

        PasswordResetOtp row = otpRepository
                .findFirstByEmailIgnoreCaseOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, invalid));

        if (!row.isOtpUsed()
                || row.getResetTokenHash() == null
                || row.getResetExpiresAt() == null
                || row.getResetExpiresAt().isBefore(LocalDateTime.now())
                || !passwordEncoder.matches(request.getResetToken(), row.getResetTokenHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, invalid);
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, invalid));

        user.setPassword(passwordEncoder.encode(request.getNewPassword())); // BCrypt
        userRepository.save(user);

        otpRepository.deleteByEmailIgnoreCase(email); // single use

        return "Password reset successfully.";
    }
}
