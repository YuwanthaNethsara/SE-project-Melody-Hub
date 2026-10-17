package com.example.project1.repository;

import com.example.project1.entity.FeaturedProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FeaturedProductRepository
        extends JpaRepository<FeaturedProduct, Long> {

    Optional<FeaturedProduct> findByProduct_ProductId(Long productId);

    boolean existsByProduct_ProductId(Long productId);

    List<FeaturedProduct> findByStatus(String status);
}
