package com.example.project1.service;

import com.example.project1.dto.OrderItemRequest;
import com.example.project1.dto.OrderItemResponse;

import java.util.List;

public interface OrderItemService {

    OrderItemResponse createOrderItem(
            OrderItemRequest request
    );

    List<OrderItemResponse> getAllOrderItems();

    OrderItemResponse getOrderItemById(Long id);

    List<OrderItemResponse> getItemsByOrder(Long orderId);

    /** Items that belong to the orders of one customer (by e-mail). */
    List<OrderItemResponse> getItemsByUserEmail(String email);

    /** E-mail of the customer who owns an order item, or null if it does not exist. */
    String getOwnerEmail(Long orderItemId);

    /** E-mail of the customer who owns an order, or null if it does not exist. */
    String getOrderOwnerEmail(Long orderId);

    OrderItemResponse updateQuantity(
            Long id,
            Integer quantity
    );

    String deleteOrderItem(Long id);
}
