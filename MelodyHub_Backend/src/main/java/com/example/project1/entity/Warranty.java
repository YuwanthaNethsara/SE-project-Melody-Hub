package com.example.project1.entity;

import com.example.project1.enums.WarrantyStatus;
import com.example.project1.enums.WarrantyType;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "warranties")
public class Warranty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long warrantyId;

    @OneToOne
    @JoinColumn(
            name = "product_id",
            nullable = false,
            unique = true
    )
    private Product product;

    @Column(nullable = false)
    private Integer warrantyPeriodMonths;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WarrantyType warrantyType;

    @Column(length = 1000)
    private String termsAndConditions;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WarrantyStatus status;

    @Column(nullable = false)
    private LocalDateTime createdDate;

    // Default constructor
    public Warranty() {
    }

    // Getters and Setters

    public Long getWarrantyId() {
        return warrantyId;
    }

    public void setWarrantyId(Long warrantyId) {
        this.warrantyId = warrantyId;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
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