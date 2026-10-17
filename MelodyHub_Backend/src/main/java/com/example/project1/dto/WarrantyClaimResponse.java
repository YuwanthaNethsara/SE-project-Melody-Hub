package com.example.project1.dto;

import com.example.project1.enums.WarrantyClaimStatus;
import com.example.project1.enums.WarrantyClaimType;

import java.time.LocalDateTime;

public class WarrantyClaimResponse {

    private Long claimId;

    private Long orderItemId;

    private Long orderId;

    private Long productId;

    private String productName;

    private Long warrantyId;

    private Long userId;

    private String userName;

    private String userEmail;

    private WarrantyClaimType claimType;

    private String reason;

    private String description;

    private LocalDateTime claimDate;

    private WarrantyClaimStatus status;

    private String resolution;

    public WarrantyClaimResponse() {
    }

    public WarrantyClaimResponse(
            Long claimId,
            Long orderItemId,
            Long orderId,
            Long productId,
            String productName,
            Long warrantyId,
            Long userId,
            String userName,
            String userEmail,
            WarrantyClaimType claimType,
            String reason,
            String description,
            LocalDateTime claimDate,
            WarrantyClaimStatus status,
            String resolution
    ) {
        this.claimId = claimId;
        this.orderItemId = orderItemId;
        this.orderId = orderId;
        this.productId = productId;
        this.productName = productName;
        this.warrantyId = warrantyId;
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.claimType = claimType;
        this.reason = reason;
        this.description = description;
        this.claimDate = claimDate;
        this.status = status;
        this.resolution = resolution;
    }

    public Long getClaimId() {
        return claimId;
    }

    public void setClaimId(Long claimId) {
        this.claimId = claimId;
    }

    public Long getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(Long orderItemId) {
        this.orderItemId = orderItemId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Long getWarrantyId() {
        return warrantyId;
    }

    public void setWarrantyId(Long warrantyId) {
        this.warrantyId = warrantyId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public WarrantyClaimType getClaimType() {
        return claimType;
    }

    public void setClaimType(WarrantyClaimType claimType) {
        this.claimType = claimType;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getClaimDate() {
        return claimDate;
    }

    public void setClaimDate(LocalDateTime claimDate) {
        this.claimDate = claimDate;
    }

    public WarrantyClaimStatus getStatus() {
        return status;
    }

    public void setStatus(WarrantyClaimStatus status) {
        this.status = status;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }
}
