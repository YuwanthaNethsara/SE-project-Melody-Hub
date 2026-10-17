package com.example.project1.service;

import com.example.project1.dto.CartItemRequest;
import com.example.project1.dto.CartResponse;

public interface CartService {
    CartResponse getMyCart(String email);
    CartResponse addItem(String email, CartItemRequest request);
    CartResponse updateItem(String email, Long cartItemId, Integer quantity);
    String removeItem(String email, Long cartItemId);
    String clearCart(String email);
}
