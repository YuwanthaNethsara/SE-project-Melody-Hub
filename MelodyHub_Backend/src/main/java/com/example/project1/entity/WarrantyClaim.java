package com.example.project1.entity;

import com.example.project1.enums.WarrantyClaimStatus;
import com.example.project1.enums.WarrantyClaimType;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "warranty_claims")
public class WarrantyClaim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long claimId;

    // Purchased Order Item
    @ManyToOne
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    // Warranty of the purchased product
    @ManyToOne
    @JoinColumn(name = "warranty_id", nullable = false)
    private Warranty warranty;

    // Customer who made the claim
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WarrantyClaimType claimType;

    @Column(nullable = false, length = 1000)
    private String reason;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private LocalDateTime claimDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WarrantyClaimStatus status;

    @Column(length = 1000)
    private String resolution;

    public WarrantyClaim() {
    }

    public Long getClaimId() {
        return claimId;
    }

    public void setClaimId(Long claimId) {
        this.claimId = claimId;
    }

    public OrderItem getOrderItem() {
        return orderItem;
    }

    public void setOrderItem(OrderItem orderItem) {
        this.orderItem = orderItem;
    }

    public Warranty getWarranty() {
        return warranty;
    }

    public void setWarranty(Warranty warranty) {
        this.warranty = warranty;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public LocalDateTime getClaimDate() {
        return claimDate;
    }

    public void setClaimDate(LocalDateTime claimDate) {
        this.claimDate = claimDate;
    }

    public WarrantyClaimStatus getStatus() {
        return status;
    }

    public void setStatus(WarrantyClaimStatus status) {
        this.status = status;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }
}
