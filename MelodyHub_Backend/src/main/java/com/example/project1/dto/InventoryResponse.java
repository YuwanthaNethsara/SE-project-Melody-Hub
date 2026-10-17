package com.example.project1.dto;

import com.example.project1.enums.StockStatus;

import java.time.LocalDateTime;

public class InventoryResponse {

    private Long inventoryId;
    private Long productId;
    private String productName;
    private String locationCode;
    private Integer quantityAvailable;
    private Integer reorderLevel;
    private StockStatus stockStatus;
    private LocalDateTime lastUpdated;

    public InventoryResponse() {
    }

    public InventoryResponse(
            Long inventoryId,
            Long productId,
            String productName,
            String locationCode,
            Integer quantityAvailable,
            Integer reorderLevel,
            StockStatus stockStatus,
            LocalDateTime lastUpdated) {

        this.inventoryId = inventoryId;
        this.productId = productId;
        this.productName = productName;
        this.locationCode = locationCode;
        this.quantityAvailable = quantityAvailable;
        this.reorderLevel = reorderLevel;
        this.stockStatus = stockStatus;
        this.lastUpdated = lastUpdated;
    }

    public Long getInventoryId() {
        return inventoryId;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public String getLocationCode() {
        return locationCode;
    }

    public Integer getQuantityAvailable() {
        return quantityAvailable;
    }

    public Integer getReorderLevel() {
        return reorderLevel;
    }

    public StockStatus getStockStatus() {
        return stockStatus;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }
}
