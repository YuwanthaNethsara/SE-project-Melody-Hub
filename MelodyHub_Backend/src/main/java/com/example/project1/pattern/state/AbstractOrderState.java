package com.example.project1.pattern.state;

import com.example.project1.entity.Order;

/** Default behaviour: every move is rejected. Concrete states override only what they allow. */
abstract class AbstractOrderState implements OrderState {

    @Override
    public void confirm(Order order) {
        reject("changed to CONFIRMED");
    }

    @Override
    public void process(Order order) {
        reject("changed to PROCESSING");
    }

    @Override
    public void ship(Order order) {
        reject("changed to SHIPPED");
    }

    @Override
    public void deliver(Order order) {
        reject("changed to DELIVERED");
    }

    @Override
    public void cancel(Order order) {
        reject("cancelled");
    }

    @Override
    public void transitionTo(Order order, String targetStatus) {
        switch (targetStatus) {
            case "CONFIRMED" -> confirm(order);
            case "PROCESSING" -> process(order);
            case "SHIPPED" -> ship(order);
            case "DELIVERED" -> deliver(order);
            case "CANCELLED" -> cancel(order);
            default -> reject("changed to " + targetStatus);   // e.g. back to PENDING
        }
    }

    protected void moveTo(Order order, OrderState next) {
        order.setOrderStatus(next.name());
    }

    protected void reject(String what) {
        throw new IllegalStateException("An order that is " + name() + " cannot be " + what);
    }
}
