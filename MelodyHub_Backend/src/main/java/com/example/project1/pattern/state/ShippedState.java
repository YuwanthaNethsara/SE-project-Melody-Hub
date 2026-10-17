package com.example.project1.pattern.state;

import com.example.project1.entity.Order;

/** On its way to the customer - it can no longer be cancelled, only delivered. */
public class ShippedState extends AbstractOrderState {
    @Override public String name() { return "SHIPPED"; }
    @Override public void deliver(Order order) { moveTo(order, OrderStates.DELIVERED); }
}
