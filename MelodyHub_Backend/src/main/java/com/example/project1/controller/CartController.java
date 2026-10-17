package com.example.project1.controller;

import com.example.project1.dto.CartItemRequest;
import com.example.project1.dto.CartResponse;
import com.example.project1.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    private final CartService cartService;
    public CartController(CartService cartService) { this.cartService = cartService; }

    @GetMapping
    public ResponseEntity<CartResponse> getMyCart(Authentication authentication) {
        return ResponseEntity.ok(cartService.getMyCart(authentication.getName()));
    }

    @PostMapping("/items")
    public ResponseEntity<Object> addItem(Authentication authentication, @RequestBody CartItemRequest request) {
        return ResponseEntity.ok(cartService.addItem(authentication.getName(), request));
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<Object> updateItem(Authentication authentication, @PathVariable Long id, @RequestParam Integer quantity) {
        return ResponseEntity.ok(cartService.updateItem(authentication.getName(), id, quantity));
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<Object> removeItem(Authentication authentication, @PathVariable Long id) {
        return ResponseEntity.ok(cartService.removeItem(authentication.getName(), id));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<String> clearCart(Authentication authentication) {
        return ResponseEntity.ok(cartService.clearCart(authentication.getName()));
    }
}
