package com.example.project1.dto;

import com.example.project1.enums.WarrantyStatus;
import com.example.project1.enums.WarrantyType;

import java.time.LocalDateTime;

public class WarrantyResponse {

    private Long warrantyId;

    private Long productId;

    private String productName;

    private Integer warrantyPeriodMonths;

    private WarrantyType warrantyType;

    private String termsAndConditions;

    private WarrantyStatus status;

    private LocalDateTime createdDate;

    public WarrantyResponse() {
    }

    public WarrantyResponse(
            Long warrantyId,
            Long productId,
            String productName,
            Integer warrantyPeriodMonths,
            WarrantyType warrantyType,
            String termsAndConditions,
            WarrantyStatus status,
            LocalDateTime createdDate
    ) {
        this.warrantyId = warrantyId;
        this.productId = productId;
        this.productName = productName;
        this.warrantyPeriodMonths = warrantyPeriodMonths;
        this.warrantyType = warrantyType;
        this.termsAndConditions = termsAndConditions;
        this.status = status;
        this.createdDate = createdDate;
    }

    public Long getWarrantyId() {
        return warrantyId;
    }

    public void setWarrantyId(Long warrantyId) {
        this.warrantyId = warrantyId;
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

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
}
