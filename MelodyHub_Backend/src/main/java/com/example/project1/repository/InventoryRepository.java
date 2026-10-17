package com.example.project1.repository;

import com.example.project1.entity.Inventory;
import com.example.project1.enums.StockStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByProduct_ProductId(Long productId);

    List<Inventory> findByStockStatus(StockStatus stockStatus);

    boolean existsByProduct_ProductId(Long productId);

    // Row lock so two simultaneous checkouts cannot sell the same last unit
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Inventory i where i.product.productId = :productId")
    Optional<Inventory> findForUpdate(@Param("productId") Long productId);
}
