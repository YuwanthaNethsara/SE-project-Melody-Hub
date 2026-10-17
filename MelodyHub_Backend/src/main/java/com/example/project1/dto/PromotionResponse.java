package com.example.project1.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class PromotionResponse {

    private Long promotionId;

    private String promotionName;

    private String description;

    private Long productId;

    private String productName;

    private Double originalPrice;

    private Double discountPercentage;

    private Double discountedPrice;

    private Double discountAmount;

    private String discountType;

    private LocalDate startDate;

    private LocalDate endDate;

    private String status;

    private LocalDateTime createdDate;

    public PromotionResponse() {
    }

    public PromotionResponse(
            Long promotionId,
            String promotionName,
            String description,
            Long productId,
            String productName,
            Double originalPrice,
            Double discountPercentage,
            Double discountedPrice,
            LocalDate startDate,
            LocalDate endDate,
            String status,
            LocalDateTime createdDate
    ) {
        this.promotionId = promotionId;
        this.promotionName = promotionName;
        this.description = description;
        this.productId = productId;
        this.productName = productName;
        this.originalPrice = originalPrice;
        this.discountPercentage = discountPercentage;
        this.discountedPrice = discountedPrice;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.createdDate = createdDate;
    }

    public Long getPromotionId() {
        return promotionId;
    }

    public void setPromotionId(Long promotionId) {
        this.promotionId = promotionId;
    }

    public String getPromotionName() {
        return promotionName;
    }

    public void setPromotionName(String promotionName) {
        this.promotionName = promotionName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public Double getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(Double originalPrice) {
        this.originalPrice = originalPrice;
    }

    public Double getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(Double discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public Double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(Double discountAmount) {
        this.discountAmount = discountAmount;
    }

    public String getDiscountType() {
        return discountType;
    }

    public void setDiscountType(String discountType) {
        this.discountType = discountType;
    }

    public Double getDiscountedPrice() {
        return discountedPrice;
    }

    public void setDiscountedPrice(Double discountedPrice) {
        this.discountedPrice = discountedPrice;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
}
