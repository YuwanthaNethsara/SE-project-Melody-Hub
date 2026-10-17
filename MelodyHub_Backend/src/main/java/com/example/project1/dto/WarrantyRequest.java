package com.example.project1.dto;

import com.example.project1.enums.WarrantyStatus;

import com.example.project1.enums.WarrantyType;

public class WarrantyRequest {

    private Long productId;

    private Integer warrantyPeriodMonths;

    private WarrantyType warrantyType;

    private String termsAndConditions;

    // optional: ACTIVE / INACTIVE / EXPIRED (defaults to ACTIVE on create)
    private WarrantyStatus status;

    public WarrantyRequest() {
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getWarrantyPeriodMonths() {
        return warrantyPeriodMonths;
    }

    public void setWarrantyPeriodMonths(Integer warrantyPeriodMonths) {
        this.warrantyPeriodMonths = warrantyPeriodMonths;
    }

    public WarrantyType getWarrantyType() {
        return warrantyType;
    }

    public void setWarrantyType(WarrantyType warrantyType) {
        this.warrantyType = warrantyType;
    }

    public String getTermsAndConditions() {
        return termsAndConditions;
    }

    public void setTermsAndConditions(String termsAndConditions) {
        this.termsAndConditions = termsAndConditions;
    }

    public WarrantyStatus getStatus() {
        return status;
    }

    public void setStatus(WarrantyStatus status) {
        this.status = status;
    }
}
