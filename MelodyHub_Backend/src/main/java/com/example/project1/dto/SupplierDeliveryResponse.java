package com.example.project1.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class SupplierDeliveryResponse {

    private Long deliveryId;
    private Long purchaseOrderId;
    private String supplierName;
    private String productName;
    private Integer orderedQuantity;
    private LocalDate deliveryDate;
    private Integer quantityReceived;
    private String status;
    private String notes;
    private LocalDateTime createdDate;

    public SupplierDeliveryResponse() {
    }

    public SupplierDeliveryResponse(Long deliveryId, Long purchaseOrderId, String supplierName,
                                    String productName, Integer orderedQuantity, LocalDate deliveryDate,
                                    Integer quantityReceived, String status, String notes,
                                    LocalDateTime createdDate) {
        this.deliveryId = deliveryId;
        this.purchaseOrderId = purchaseOrderId;
        this.supplierName = supplierName;
        this.productName = productName;
        this.orderedQuantity = orderedQuantity;
        this.deliveryDate = deliveryDate;
        this.quantityReceived = quantityReceived;
        this.status = status;
        this.notes = notes;
        this.createdDate = createdDate;
    }

    public Long getDeliveryId() {
        return deliveryId;
    }

    public Long getPurchaseOrderId() {
        return purchaseOrderId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public String getProductName() {
        return productName;
    }

    public Integer getOrderedQuantity() {
        return orderedQuantity;
    }

    public LocalDate getDeliveryDate() {
        return deliveryDate;
    }

    public Integer getQuantityReceived() {
        return quantityReceived;
    }

    public String getStatus() {
        return status;
    }

    public String getNotes() {
        return notes;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
}
