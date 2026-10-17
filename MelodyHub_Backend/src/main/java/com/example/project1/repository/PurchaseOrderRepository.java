package com.example.project1.repository;

import com.example.project1.entity.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseOrderRepository
        extends JpaRepository<PurchaseOrder, Long> {

    List<PurchaseOrder> findBySupplier_SupplierId(
            Long supplierId
    );

    List<PurchaseOrder> findByProduct_ProductId(
            Long productId
    );

    boolean existsByProduct_ProductId(Long productId);

    boolean existsBySupplier_SupplierId(Long supplierId);

    List<PurchaseOrder> findByStatus(
            String status
    );
}
