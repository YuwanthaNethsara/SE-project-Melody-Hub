package com.example.project1.controller;

import com.example.project1.dto.CheckoutRequest;
import com.example.project1.dto.OrderRequest;
import com.example.project1.dto.OrderResponse;
import com.example.project1.dto.ShippingAddressRequest;
import com.example.project1.entity.User;
import com.example.project1.repository.UserRepository;
import com.example.project1.service.OrderService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Role rules (also see SecurityConfig):
 *  CUSTOMER        - create own order from the cart, see/cancel own orders only
 *  SALES_MANAGER   - see all orders, move orders through their statuses
 *  ADMIN           - everything (create for a customer, delete, payment status)
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final UserRepository userRepository;

    public OrderController(OrderService orderService, UserRepository userRepository) {
        this.orderService = orderService;
        this.userRepository = userRepository;
    }

    // Admin: order on behalf of a customer. Customer: checks out their own cart.
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest request,
                                                     Authentication authentication) {
        if (isAdmin(authentication)) {
            return ResponseEntity.ok(orderService.createOrder(request));
        }
        if (isStaff(authentication)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You do not have permission to perform this action.");
        }
        return ResponseEntity.ok(
                orderService.checkoutCart(authentication.getName(), request.getShippingAddress()));
    }

    @PostMapping("/checkout")
    public ResponseEntity<OrderResponse> checkout(@RequestBody CheckoutRequest request,
                                                  Authentication authentication) {
        if (isStaff(authentication)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only customers can check out a cart.");
        }
        return ResponseEntity.ok(
                orderService.checkoutCart(authentication.getName(), request.getShippingAddress()));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getOrders(Authentication authentication) {
        if (isStaff(authentication)) {
            return ResponseEntity.ok(orderService.getAllOrders());
        }
        return ResponseEntity.ok(orderService.getOrdersByUser(currentUser(authentication).getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id,
                                                      Authentication authentication) {
        OrderResponse response = orderService.getOrderById(id);
        if (response == null) return ResponseEntity.notFound().build();
        if (!isStaff(authentication) && !response.getUserEmail().equalsIgnoreCase(authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot access another user's order");
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByUser(@PathVariable Long userId,
                                                               Authentication authentication) {
        if (!isStaff(authentication) && !currentUser(authentication).getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only view your own orders");
        }
        return ResponseEntity.ok(orderService.getOrdersByUser(userId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderResponse>> getOrdersByStatus(@PathVariable String status,
                                                                 Authentication authentication) {
        requireStaff(authentication);
        return ResponseEntity.ok(orderService.getOrdersByStatus(status));
    }

    @GetMapping("/payment-status/{status}")
    public ResponseEntity<List<OrderResponse>> getOrdersByPaymentStatus(@PathVariable String status,
                                                                        Authentication authentication) {
        requireStaff(authentication);
        return ResponseEntity.ok(orderService.getOrdersByPaymentStatus(status));
    }

    // ADMIN + SALES_MANAGER
    @PutMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(@PathVariable Long id,
                                                           @RequestParam String status,
                                                           Authentication authentication) {
        requireStaff(authentication);
        OrderResponse response = orderService.updateOrderStatus(id, status);
        if (response == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(response);
    }

    // ADMIN only
    @PutMapping("/{id}/payment-status")
    public ResponseEntity<OrderResponse> updatePaymentStatus(@PathVariable Long id,
                                                             @RequestParam String status,
                                                             Authentication authentication) {
        requireAdmin(authentication);
        OrderResponse response = orderService.updatePaymentStatus(id, status);
        if (response == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(response);
    }

    // Customer: own PENDING order. Staff: any order that has not shipped.
    @PutMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable Long id,
                                                     Authentication authentication) {
        return ResponseEntity.ok(
                orderService.cancelOrder(id, authentication.getName(), isStaff(authentication)));
    }

    // Customer: own order before processing starts. ADMIN: any such order.
    @PutMapping("/{id}/shipping-address")
    public ResponseEntity<OrderResponse> updateShippingAddress(@PathVariable Long id,
                                                               @RequestBody ShippingAddressRequest request,
                                                               Authentication authentication) {
        return ResponseEntity.ok(orderService.updateShippingAddress(
                id, request.getShippingAddress(), authentication.getName(), isAdmin(authentication)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteOrder(@PathVariable Long id, Authentication authentication) {
        requireAdmin(authentication);
        return ResponseEntity.ok(orderService.deleteOrder(id));
    }

    // ---------- helpers ----------
    private boolean hasRole(Authentication authentication, String role) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> role.equals(a.getAuthority()));
    }

    private boolean isAdmin(Authentication authentication) {
        return hasRole(authentication, "ROLE_ADMIN");
    }

    /** ADMIN or SALES_MANAGER: may see every order. */
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
