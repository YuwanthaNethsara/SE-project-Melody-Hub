package com.example.project1.repository;

import com.example.project1.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SupplierRepository
        extends JpaRepository<Supplier, Long> {

    Optional<Supplier> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Supplier> findByStatus(String status);

    List<Supplier> findBySupplierNameContainingIgnoreCase(
            String supplierName
    );
}
