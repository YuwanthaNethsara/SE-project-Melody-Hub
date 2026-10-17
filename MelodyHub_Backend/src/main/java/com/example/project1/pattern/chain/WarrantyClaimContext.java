package com.example.project1.pattern.chain;

import com.example.project1.dto.WarrantyClaimRequest;
import com.example.project1.entity.Order;
import com.example.project1.entity.OrderItem;
import com.example.project1.entity.User;
import com.example.project1.entity.Warranty;

/** The claim that travels along the chain; handlers read it and fill in what they discover. */
public class WarrantyClaimContext {

    private final WarrantyClaimRequest request;
    private final User user;
    private OrderItem orderItem;
    private Order order;
    private Warranty warranty;

    public WarrantyClaimContext(WarrantyClaimRequest request, User user) {
        this.request = request;
        this.user = user;
    }

    public WarrantyClaimRequest getRequest() { return request; }
    public User getUser() { return user; }
    public OrderItem getOrderItem() { return orderItem; }
    public void setOrderItem(OrderItem orderItem) { this.orderItem = orderItem; this.order = orderItem.getOrder(); }
    public Order getOrder() { return order; }
    public Warranty getWarranty() { return warranty; }
    public void setWarranty(Warranty warranty) { this.warranty = warranty; }
}
