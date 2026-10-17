package com.example.project1.entity;

import com.example.project1.pattern.state.OrderState;
import com.example.project1.pattern.state.OrderStates;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    // Customer/User who placed the order
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime orderDate;

    @Column(nullable = false)
    private Double totalAmount;

    @Column(nullable = false)
    private String orderStatus;

    @Column(nullable = false)
    private String paymentStatus;

    @Column(nullable = false)
    private String shippingAddress;

    public Order() {
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    // ---------- STATE pattern: the order delegates to its current lifecycle state ----------
    // (the stored column is still the plain orderStatus text, so the database is unchanged)

    public OrderState currentState() {
        return OrderStates.of(orderStatus);
    }

    public void confirm() {
        currentState().confirm(this);
    }

    public void process() {
        currentState().process(this);
    }

    public void ship() {
        currentState().ship(this);
    }

    public void deliver() {
        currentState().deliver(this);
    }

    public void cancel() {
        currentState().cancel(this);
    }
}
