package com.example.project1.repository;

import com.example.project1.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PromotionRepository
        extends JpaRepository<Promotion, Long> {

    List<Promotion> findByProduct_ProductId(Long productId);

    List<Promotion> findByStatus(String status);

    List<Promotion> findByPromotionNameContainingIgnoreCase(
            String promotionName
    );
}
