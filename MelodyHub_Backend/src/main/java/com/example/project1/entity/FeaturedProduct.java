package com.example.project1.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "featured_products")
public class FeaturedProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long featuredProductId;

    @OneToOne
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private Product product;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private LocalDateTime featuredDate;

    public FeaturedProduct() {
    }

    public Long getFeaturedProductId() {
        return featuredProductId;
    }

    public void setFeaturedProductId(Long featuredProductId) {
        this.featuredProductId = featuredProductId;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
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
