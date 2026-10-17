package com.example.project1.dto;

public class PhysicalProductRequest {

    private Long productId;
    private Double weight;
    private String shoppingClass;

    public PhysicalProductRequest() {
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public String getShoppingClass() {
        return shoppingClass;
    }

    public void setShoppingClass(String shoppingClass) {
        this.shoppingClass = shoppingClass;
    }
}
