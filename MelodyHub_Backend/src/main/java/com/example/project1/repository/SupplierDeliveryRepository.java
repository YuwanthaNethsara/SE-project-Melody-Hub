package com.example.project1.repository;

import com.example.project1.entity.SupplierDelivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupplierDeliveryRepository extends JpaRepository<SupplierDelivery, Long> {

    List<SupplierDelivery> findByPurchaseOrder_PurchaseOrderId(Long purchaseOrderId);

    boolean existsByPurchaseOrder_PurchaseOrderId(Long purchaseOrderId);
}
