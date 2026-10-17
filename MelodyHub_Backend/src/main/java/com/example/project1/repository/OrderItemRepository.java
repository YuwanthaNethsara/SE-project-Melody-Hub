package com.example.project1.repository;

import com.example.project1.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository
        extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrder_OrderId(Long orderId);

    List<OrderItem> findByProduct_ProductId(Long productId);

    boolean existsByProduct_ProductId(Long productId);

    // items that belong to one customer's orders
    List<OrderItem> findByOrder_User_Email(String email);
}
