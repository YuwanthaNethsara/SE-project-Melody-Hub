package com.example.project1.repository;

import com.example.project1.entity.SupplierContract;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupplierContractRepository
        extends JpaRepository<SupplierContract, Long> {

    List<SupplierContract> findBySupplier_SupplierId(
            Long supplierId
    );

    boolean existsBySupplier_SupplierId(Long supplierId);

    List<SupplierContract> findByStatus(
            String status
    );

    List<SupplierContract>
    findByContractNameContainingIgnoreCase(
            String contractName
    );
}
