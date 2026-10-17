package com.example.project1.pattern.state;

import com.example.project1.entity.Order;

/** The shop is preparing the items. */
public class ProcessingState extends AbstractOrderState {
    @Override public String name() { return "PROCESSING"; }
    @Override public void ship(Order order) { moveTo(order, OrderStates.SHIPPED); }
    @Override public void cancel(Order order) { moveTo(order, OrderStates.CANCELLED); }
}
