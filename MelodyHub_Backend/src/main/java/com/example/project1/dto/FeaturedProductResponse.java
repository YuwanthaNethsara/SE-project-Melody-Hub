package com.example.project1.dto;

import java.time.LocalDateTime;

public class FeaturedProductResponse {

    private Long featuredProductId;
    private Long productId;
    private String productName;
    private Double price;
    private String imageUrl;
    private String status;
    private LocalDateTime featuredDate;

    public FeaturedProductResponse() {
    }

    public FeaturedProductResponse(
            Long featuredProductId,
            Long productId,
            String productName,
            Double price,
            String imageUrl,
            String status,
            LocalDateTime featuredDate
    ) {
        this.featuredProductId = featuredProductId;
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.imageUrl = imageUrl;
        this.status = status;
        this.featuredDate = featuredDate;
    }

    public Long getFeaturedProductId() {
        return featuredProductId;
    }

    public void setFeaturedProductId(Long featuredProductId) {
        this.featuredProductId = featuredProductId;
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

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getFeaturedDate() {
        return featuredDate;
    }

    public void setFeaturedDate(LocalDateTime featuredDate) {
        this.featuredDate = featuredDate;
    }
}
