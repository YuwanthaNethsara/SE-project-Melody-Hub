package com.example.project1.pattern.state;

/** Final state: stock was returned and any payment refunded by OrderServiceImpl. */
public class CancelledState extends AbstractOrderState {
    @Override public String name() { return "CANCELLED"; }
}
