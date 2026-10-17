package com.example.project1.dto;

import java.util.ArrayList;
import java.util.List;

public class CartResponse {
    private Long cartId;
    private Long userId;
    private List<CartItemResponse> items = new ArrayList<>();
    private Double totalAmount = 0.0;

    public CartResponse() {}

    public CartResponse(Long cartId, Long userId, List<CartItemResponse> items, Double totalAmount) {
        this.cartId = cartId;
        this.userId = userId;
        this.items = items;
        this.totalAmount = totalAmount;
    }

    public Long getCartId() { return cartId; }
    public void setCartId(Long cartId) { this.cartId = cartId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public List<CartItemResponse> getItems() { return items; }
    public void setItems(List<CartItemResponse> items) { this.items = items; }
    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
}
