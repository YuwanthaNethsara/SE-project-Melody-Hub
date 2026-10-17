package com.example.project1.pattern.state;

import com.example.project1.entity.Order;

/** New order, waiting for payment/confirmation. Can be confirmed, processed directly, or cancelled. */
public class PendingState extends AbstractOrderState {
    @Override public String name() { return "PENDING"; }
    @Override public void confirm(Order order) { moveTo(order, OrderStates.CONFIRMED); }
    @Override public void process(Order order) { moveTo(order, OrderStates.PROCESSING); }
    @Override public void cancel(Order order) { moveTo(order, OrderStates.CANCELLED); }
}
