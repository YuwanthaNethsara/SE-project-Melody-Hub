package com.example.project1.repository;

import com.example.project1.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCart_CartId(Long cartId);

    List<CartItem> findByProduct_ProductId(Long productId);
    Optional<CartItem> findByCart_CartIdAndProduct_ProductId(Long cartId, Long productId);
}
