package com.example.project1.repository;

import com.example.project1.entity.WarrantyClaim;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WarrantyClaimRepository
        extends JpaRepository<WarrantyClaim, Long> {

    List<WarrantyClaim> findByUser_Id(Long userId);

    List<WarrantyClaim> findByStatus(
            com.example.project1.enums.WarrantyClaimStatus status
    );

    boolean existsByOrderItem_OrderItemId(Long orderItemId);

    boolean existsByWarranty_WarrantyId(Long warrantyId);

    boolean existsByOrderItem_Order_OrderId(Long orderId);
}
