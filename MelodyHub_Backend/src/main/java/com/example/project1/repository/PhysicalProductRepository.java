package com.example.project1.repository;

import com.example.project1.entity.PhysicalProduct;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhysicalProductRepository
        extends JpaRepository<PhysicalProduct, Long> {
}
