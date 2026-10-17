package com.example.project1.dto;

import com.example.project1.enums.WarrantyClaimStatus;
import com.example.project1.enums.WarrantyType;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * One purchased order item that has a warranty, seen from the customer's side.
 * "claimable" tells the UI whether a new claim can be submitted right now.
 */
public class EligibleWarrantyResponse {

    private Long orderItemId;
    private Long orderId;
    private LocalDateTime purchaseDate;
    private Long productId;
    private String productName;
    private Integer quantity;
    private Long warrantyId;
    private Integer warrantyPeriodMonths;
    private WarrantyType warrantyType;
    private String termsAndConditions;
    private LocalDate expiresOn;
    private boolean claimable;
    private String notClaimableReason;
    private WarrantyClaimStatus claimStatus;

    public EligibleWarrantyResponse() {
    }

    public Long getOrderItemId() { return orderItemId; }
    public void setOrderItemId(Long orderItemId) { this.orderItemId = orderItemId; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public LocalDateTime getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDateTime purchaseDate) { this.purchaseDate = purchaseDate; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Long getWarrantyId() { return warrantyId; }
    public void setWarrantyId(Long warrantyId) { this.warrantyId = warrantyId; }

    public Integer getWarrantyPeriodMonths() { return warrantyPeriodMonths; }
    public void setWarrantyPeriodMonths(Integer warrantyPeriodMonths) { this.warrantyPeriodMonths = warrantyPeriodMonths; }

    public WarrantyType getWarrantyType() { return warrantyType; }
    public void setWarrantyType(WarrantyType warrantyType) { this.warrantyType = warrantyType; }

    public String getTermsAndConditions() { return termsAndConditions; }
    public void setTermsAndConditions(String termsAndConditions) { this.termsAndConditions = termsAndConditions; }

    public LocalDate getExpiresOn() { return expiresOn; }
    public void setExpiresOn(LocalDate expiresOn) { this.expiresOn = expiresOn; }

    public boolean isClaimable() { return claimable; }
    public void setClaimable(boolean claimable) { this.claimable = claimable; }

    public String getNotClaimableReason() { return notClaimableReason; }
    public void setNotClaimableReason(String notClaimableReason) { this.notClaimableReason = notClaimableReason; }

    public WarrantyClaimStatus getClaimStatus() { return claimStatus; }
    public void setClaimStatus(WarrantyClaimStatus claimStatus) { this.claimStatus = claimStatus; }
}
