package com.example.project1.service;

import com.example.project1.dto.OrderRequest;
import com.example.project1.dto.OrderResponse;

import java.util.List;

public interface OrderService {

    OrderResponse createOrder(OrderRequest request);

    OrderResponse checkoutCart(String email, String shippingAddress);

    List<OrderResponse> getAllOrders();

    OrderResponse getOrderById(Long id);

    List<OrderResponse> getOrdersByUser(Long userId);

    List<OrderResponse> getOrdersByStatus(String status);

    List<OrderResponse> getOrdersByPaymentStatus(String status);

    OrderResponse updateOrderStatus(Long id, String status);

    OrderResponse updatePaymentStatus(Long id, String status);

    /** Cancel an order (stock is returned, a paid order is marked refunded). */
    OrderResponse cancelOrder(Long id, String requesterEmail, boolean staff);

    OrderResponse updateShippingAddress(Long id, String shippingAddress, String requesterEmail, boolean staff);

    String deleteOrder(Long id);
}
