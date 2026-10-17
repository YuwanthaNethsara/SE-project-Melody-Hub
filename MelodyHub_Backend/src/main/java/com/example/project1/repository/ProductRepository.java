package com.example.project1.repository;

import com.example.project1.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByNameContainingIgnoreCase(String name);

    List<Product> findByCategory_CategoryId(Long categoryId);

    Optional<Product> findBySku(String sku);

    boolean existsByCategory_CategoryId(Long categoryId);
}