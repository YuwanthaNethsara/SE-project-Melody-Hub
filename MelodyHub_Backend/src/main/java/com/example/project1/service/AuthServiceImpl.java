package com.example.project1.service;

import com.example.project1.dto.CreateUserRequest;
import com.example.project1.dto.LoginRequest;
import com.example.project1.dto.LoginResponse;
import com.example.project1.dto.RegisterRequest;
import com.example.project1.dto.UpdateUserRequest;
import com.example.project1.dto.UserResponse;
import com.example.project1.entity.User;
import com.example.project1.enums.Role;
import com.example.project1.repository.ArtistFollowerRepository;
import com.example.project1.repository.CartItemRepository;
import com.example.project1.repository.CartRepository;
import com.example.project1.repository.NotificationRepository;
import com.example.project1.repository.OrderRepository;
import com.example.project1.repository.ReviewRepository;
import com.example.project1.repository.UserRepository;
import com.example.project1.repository.WarrantyClaimRepository;
import org.springframework.transaction.annotation.Transactional;
import com.example.project1.security.JwtService;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    // used to clean up / protect related records when an admin deletes a user
    private final OrderRepository orderRepository;
    private final WarrantyClaimRepository warrantyClaimRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final NotificationRepository notificationRepository;
    private final ArtistFollowerRepository artistFollowerRepository;
    private final ReviewRepository reviewRepository;

    public AuthServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            OrderRepository orderRepository,
            WarrantyClaimRepository warrantyClaimRepository,
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            NotificationRepository notificationRepository,
            ArtistFollowerRepository artistFollowerRepository,
            ReviewRepository reviewRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.orderRepository = orderRepository;
        this.warrantyClaimRepository = warrantyClaimRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.notificationRepository = notificationRepository;
        this.artistFollowerRepository = artistFollowerRepository;
        this.reviewRepository = reviewRepository;
    }

    // =========================
    // REGISTER (public) -> always CUSTOMER
    // =========================
    @Override
    public String register(RegisterRequest request) {

        User user = buildUser(
                request.getName(),
                request.getEmail(),
                request.getPassword(),
                Role.CUSTOMER);

        userRepository.save(user);

        return "Registration successful";
    }

    // =========================
    // CREATE USER (ADMIN) -> any role
    // =========================
    @Override
    public UserResponse createUser(CreateUserRequest request) {

        if (request.getRole() == null) {
            throw new IllegalArgumentException("Role is required");
        }

        User user = buildUser(
                request.getName(),
                request.getEmail(),
                request.getPassword(),
                request.getRole());

        return convertToUserResponse(userRepository.save(user));
    }

    private User buildUser(String name, String email, String password, Role role) {

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required");
        }

        validateEmail(email);
        validatePassword(password);

        String cleanEmail = email.trim();

        if (userRepository.existsByEmail(cleanEmail)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Email already registered");
        }

        User user = new User();
        user.setName(name.trim());
        user.setEmail(cleanEmail);
        user.setPassword(passwordEncoder.encode(password)); // BCrypt, never plain text
        user.setRole(role);
        return user;
    }

    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new IllegalArgumentException("Please enter a valid email address");
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters.");
        }
        if (password.length() > 72) {
            throw new IllegalArgumentException("Password must be at most 72 characters.");
        }
        if (!password.matches(".*[A-Za-z].*") || !password.matches(".*\\d.*")) {
            throw new IllegalArgumentException("Password must contain at least one letter and one number.");
        }
    }

    // =========================
    // LOGIN
    // =========================
    @Override
    public LoginResponse login(LoginRequest request) {

        if (request.getEmail() == null || request.getPassword() == null) {
            throw new IllegalArgumentException("Email and password are required");
        }

        User user = userRepository
                .findByEmail(request.getEmail().trim())
                .orElse(null);

        // Same message for "no such user" and "wrong password" (no user enumeration)
        if (user == null
                || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        if (user.getRole() == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "User role is not assigned");
        }

        String token = jwtService.generateToken(
                user.getEmail(),
                user.getRole().name());

        return new LoginResponse(
                "Login successful",
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                token);
    }

    // =========================
    // READ USERS
    // =========================
    @Override
    public List<UserResponse> getAllUsers() {

        return userRepository
                .findAll()
                .stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponse getUserById(Long id) {

        return userRepository
                .findById(id)
                .map(this::convertToUserResponse)
                .orElse(null);
    }

    @Override
    public UserResponse getUserByEmail(String email) {

        return userRepository
                .findByEmail(email)
                .map(this::convertToUserResponse)
                .orElse(null);
    }

    // =========================
    // UPDATE USER (ADMIN)
    // =========================
    @Override
    public String updateUser(Long id, UpdateUserRequest request) {

        User user = userRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            user.setName(request.getName().trim());
        }

        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            validateEmail(request.getEmail());
            String newEmail = request.getEmail().trim();
            if (!newEmail.equalsIgnoreCase(user.getEmail())
                    && userRepository.existsByEmail(newEmail)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT, "Email already registered");
            }
            user.setEmail(newEmail);
        }

        if (request.getRole() != null) {
            if (user.getRole() == Role.ADMIN && request.getRole() != Role.ADMIN
                    && userRepository.findByRoleIn(List.of(Role.ADMIN)).size() <= 1) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "The last administrator cannot be given another role.");
            }
            user.setRole(request.getRole());
        }

        // Update password only if provided
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            validatePassword(request.getPassword());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        userRepository.save(user);

        return "User updated successfully";
    }

    // =========================
    // UPDATE OWN PROFILE
    // =========================
    @Override
    public UserResponse updateOwnProfile(String email, UpdateUserRequest request) {

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            user.setName(request.getName().trim());
        }

        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {

            if (request.getCurrentPassword() == null
                    || !passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                throw new IllegalArgumentException("Current password is incorrect");
            }

            validatePassword(request.getPassword());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return convertToUserResponse(userRepository.save(user));
    }

    // =========================
    // DELETE USER (ADMIN)
    // =========================
    @Override
    @Transactional
    public String deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));
        // order history must never disappear: such accounts stay (change the role or password instead)
        if (!orderRepository.findByUser_Id(id).isEmpty() || !warrantyClaimRepository.findByUser_Id(id).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This user has orders or warranty claims, so the account cannot be deleted.");
        }
        if (user.getRole() == Role.ADMIN && userRepository.findByRoleIn(List.of(Role.ADMIN)).size() <= 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The last administrator account cannot be deleted.");
        }
        // records that only exist for this account go with it
        cartRepository.findByUser_Id(id).ifPresent(cart -> {
            cartItemRepository.deleteAll(cartItemRepository.findByCart_CartId(cart.getCartId()));
            cartRepository.delete(cart);
        });
        notificationRepository.deleteAll(notificationRepository.findByUser_IdOrderByCreatedAtDesc(id));
        artistFollowerRepository.deleteAll(artistFollowerRepository.findByUser_Id(id));
        reviewRepository.deleteAll(reviewRepository.findByUser_Id(id));
        userRepository.delete(user);
        return "User deleted successfully";
    }

    private UserResponse convertToUserResponse(User user) {

        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole());
    }
}
