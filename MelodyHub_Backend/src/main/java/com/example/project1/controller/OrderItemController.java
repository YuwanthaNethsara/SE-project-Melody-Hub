package com.example.project1.controller;

import com.example.project1.dto.OrderItemRequest;
import com.example.project1.dto.OrderItemResponse;
import com.example.project1.service.OrderItemService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order-items")
public class OrderItemController {

    private final OrderItemService orderItemService;

    public OrderItemController(
            OrderItemService orderItemService) {

        this.orderItemService = orderItemService;
    }

    // Add product to an order
    @PostMapping
    public ResponseEntity<?> createOrderItem(
            @RequestBody OrderItemRequest request) {

        return ResponseEntity.ok(orderItemService.createOrderItem(request));
    }

    // Get all order items
    // ADMIN / SALES_MANAGER see everything, a CUSTOMER only the items of their own orders
    @GetMapping
    public ResponseEntity<List<OrderItemResponse>>
    getAllOrderItems(Authentication authentication) {

        if (isStaff(authentication)) {
            return ResponseEntity.ok(orderItemService.getAllOrderItems());
        }

        return ResponseEntity.ok(
                orderItemService.getItemsByUserEmail(authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderItemById(
            @PathVariable Long id,
            Authentication authentication) {

        OrderItemResponse response =
                orderItemService.getOrderItemById(id);

        if (response == null) {
            return ResponseEntity.notFound().build();
        }

        requireOwnerOrStaff(
                orderItemService.getOwnerEmail(id), authentication);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<OrderItemResponse>>
    getItemsByOrder(
            @PathVariable Long orderId,
            Authentication authentication) {

        if (!isStaff(authentication)) {
            requireOwnerOrStaff(
                    orderItemService.getOrderOwnerEmail(orderId), authentication);
        }

        return ResponseEntity.ok(
                orderItemService.getItemsByOrder(orderId)
        );
    }

    private boolean isStaff(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority())
                        || "ROLE_SALES_MANAGER".equals(a.getAuthority()));
    }

    private void requireOwnerOrStaff(String ownerEmail, Authentication authentication) {
        if (isStaff(authentication)) {
            return;
        }
        if (ownerEmail == null || !ownerEmail.equalsIgnoreCase(authentication.getName())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "You cannot access another user's order");
        }
    }

    @PutMapping("/{id}/quantity")
    public ResponseEntity<?> updateQuantity(
            @PathVariable Long id,
            @RequestParam Integer quantity) {

        return ResponseEntity.ok(orderItemService.updateQuantity(id, quantity));
    }

    // Delete order item
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteOrderItem(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                orderItemService.deleteOrderItem(id)
        );
    }
}
