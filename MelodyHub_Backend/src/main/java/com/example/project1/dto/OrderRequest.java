package com.example.project1.dto;

import java.util.ArrayList;
import java.util.List;

public class OrderRequest {

    private Long userId;
    private String shippingAddress;
    private List<OrderItemRequest> items = new ArrayList<>();

    public OrderRequest() {}

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

    public List<OrderItemRequest> getItems() { return items; }
    public void setItems(List<OrderItemRequest> items) { this.items = items; }
}
