package com.example.project1.repository;

import com.example.project1.entity.Warranty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WarrantyRepository
        extends JpaRepository<Warranty, Long> {

    Optional<Warranty> findByProduct_ProductId(Long productId);

    boolean existsByProduct_ProductId(Long productId);
}
