package com.example.project1.dto;

import com.example.project1.enums.WarrantyClaimType;

public class WarrantyClaimRequest {

    private Long orderItemId;

    private WarrantyClaimType claimType;

    private String reason;

    private String description;

    public WarrantyClaimRequest() {
    }

    public Long getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(Long orderItemId) {
        this.orderItemId = orderItemId;
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
}
