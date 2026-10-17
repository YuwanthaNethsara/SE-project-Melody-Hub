package com.example.project1.controller;

import com.example.project1.dto.PaymentRequest;
import com.example.project1.dto.PaymentResponse;
import com.example.project1.entity.User;
import com.example.project1.repository.UserRepository;
import com.example.project1.service.PaymentService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * CUSTOMER      - pay for and view payments of their OWN orders only
 * SALES_MANAGER - view all payments
 * ADMIN         - everything (change status, delete)
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final UserRepository userRepository;

    public PaymentController(PaymentService paymentService, UserRepository userRepository) {
        this.paymentService = paymentService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@RequestBody PaymentRequest request,
                                                         Authentication authentication) {
        return ResponseEntity.ok(
                paymentService.createPaymentForUser(request, authentication.getName(), isAdmin(authentication)));
    }

    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getAllPayments(Authentication authentication) {
        if (isStaff(authentication)) {
            return ResponseEntity.ok(paymentService.getAllPayments());
        }
        User user = currentUser(authentication);
        return ResponseEntity.ok(paymentService.getPaymentsByUser(user.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable Long id,
                                                          Authentication authentication) {
        PaymentResponse response = paymentService.getPaymentById(id);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        if (!isStaff(authentication)
                && !paymentService.orderBelongsToUser(response.getOrderId(), currentUser(authentication).getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot access another user's payment");
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentByOrder(@PathVariable Long orderId,
                                                             Authentication authentication) {
        if (!isStaff(authentication)
                && !paymentService.orderBelongsToUser(orderId, currentUser(authentication).getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot access another user's payment");
        }
        PaymentResponse response = paymentService.getPaymentByOrder(orderId);
        // 204 = the order exists but has not been paid yet (not an error)
        if (response == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByStatus(@PathVariable String status,
                                                                     Authentication authentication) {
        requireStaff(authentication);
        return ResponseEntity.ok(paymentService.getPaymentsByStatus(status));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<PaymentResponse> updatePaymentStatus(@PathVariable Long id,
                                                               @RequestParam String status,
                                                               Authentication authentication) {
        requireAdmin(authentication);
        PaymentResponse response = paymentService.updatePaymentStatus(id, status);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePayment(@PathVariable Long id, Authentication authentication) {
        requireAdmin(authentication);
        return ResponseEntity.ok(paymentService.deletePayment(id));
    }

    // ---------- helpers ----------
    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream().anyMatch(a -> role.equals(a.getAuthority()));
    }

    private boolean isAdmin(Authentication authentication) {
        return hasRole(authentication, "ROLE_ADMIN");
    }

    private boolean isStaff(Authentication authentication) {
        return isAdmin(authentication) || hasRole(authentication, "ROLE_SALES_MANAGER");
    }

    private void requireStaff(Authentication authentication) {
        if (!isStaff(authentication)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to perform this action.");
        }
    }

    private void requireAdmin(Authentication authentication) {
        if (!isAdmin(authentication)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access required");
        }
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
