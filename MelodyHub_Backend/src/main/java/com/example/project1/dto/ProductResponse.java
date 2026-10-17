package com.example.project1.dto;

import com.example.project1.enums.ProductType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProductResponse {

    private Long productId;
    private String name;
    private String description;
    private BigDecimal price;
    private ProductType productType;
    private String status;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Long categoryId;
    private String categoryName;

    private String sku;

    // filled from inventory so the storefront can show availability (null = stock not tracked)
    private Integer quantityAvailable;
    private String stockStatus;

    public ProductResponse() {
    }

    public ProductResponse(
            Long productId,
            String name,
            String description,
            BigDecimal price,
            ProductType productType,
            String status,
            String imageUrl,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            Long categoryId,
            String categoryName) {

        this.productId = productId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.productType = productType;
        this.status = status;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public ProductType getProductType() {
        return productType;
    }

    public void setProductType(ProductType productType) {
        this.productType = productType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Integer getQuantityAvailable() {
        return quantityAvailable;
    }

    public void setQuantityAvailable(Integer quantityAvailable) {
        this.quantityAvailable = quantityAvailable;
    }

    public String getStockStatus() {
        return stockStatus;
    }

    public void setStockStatus(String stockStatus) {
        this.stockStatus = stockStatus;
    }
}
