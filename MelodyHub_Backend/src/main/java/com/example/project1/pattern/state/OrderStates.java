package com.example.project1.pattern.state;

import java.util.Map;

/** Holds the single instance of each state (they carry no data) and finds a state by its name. */
public final class OrderStates {

    public static final OrderState PENDING = new PendingState();
    public static final OrderState CONFIRMED = new ConfirmedState();
    public static final OrderState PROCESSING = new ProcessingState();
    public static final OrderState SHIPPED = new ShippedState();
    public static final OrderState DELIVERED = new DeliveredState();
    public static final OrderState CANCELLED = new CancelledState();

    private static final Map<String, OrderState> BY_NAME = Map.of(
            "PENDING", PENDING, "CONFIRMED", CONFIRMED, "PROCESSING", PROCESSING,
            "SHIPPED", SHIPPED, "DELIVERED", DELIVERED, "CANCELLED", CANCELLED);

    private OrderStates() {
    }

    public static boolean isKnown(String status) {
        return status != null && BY_NAME.containsKey(status.trim().toUpperCase());
    }

    /** A missing status means a brand-new order, i.e. PENDING. */
    public static OrderState of(String status) {
        if (status == null || status.isBlank()) {
            return PENDING;
        }
        OrderState state = BY_NAME.get(status.trim().toUpperCase());
        if (state == null) {
            throw new IllegalArgumentException("Unknown order status: " + status);
        }
        return state;
    }
}
