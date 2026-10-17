package com.example.project1.pattern.state;

import com.example.project1.entity.Order;

/** Payment received / order accepted. */
public class ConfirmedState extends AbstractOrderState {
    @Override public String name() { return "CONFIRMED"; }
    @Override public void process(Order order) { moveTo(order, OrderStates.PROCESSING); }
    @Override public void ship(Order order) { moveTo(order, OrderStates.SHIPPED); }
    @Override public void cancel(Order order) { moveTo(order, OrderStates.CANCELLED); }
}
