package com.example.project1.pattern.state;

import com.example.project1.entity.Order;

/**
 * STATE pattern (Behavioral) - Sales & Order Management.
 *
 * The behaviour of an order depends on its lifecycle stage:
 *   PENDING -> CONFIRMED -> PROCESSING -> SHIPPED -> DELIVERED   (and CANCELLED before shipping)
 * Each stage is its own class. The Order delegates confirm/process/ship/deliver/cancel to its
 * current state, and a state either performs the move or rejects it. There is no
 * "if status == ..." chain in OrderServiceImpl any more.
 */
public interface OrderState {

    /** The value stored in the existing orders.order_status column. */
    String name();

    void confirm(Order order);

    void process(Order order);

    void ship(Order order);

    void deliver(Order order);

    void cancel(Order order);

    /** Used by the REST status endpoint: runs the operation that matches the requested status. */
    void transitionTo(Order order, String targetStatus);
}
