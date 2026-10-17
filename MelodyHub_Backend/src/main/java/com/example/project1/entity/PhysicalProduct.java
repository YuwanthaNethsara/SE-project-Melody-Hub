package com.example.project1.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "physical_products")
public class PhysicalProduct {

    @Id
    private Long productId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "product_id")
    private Product product;

    private Double weight;

    private String shoppingClass;

    public PhysicalProduct() {
    }

    public Long getProductId() {
        return productId;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
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
