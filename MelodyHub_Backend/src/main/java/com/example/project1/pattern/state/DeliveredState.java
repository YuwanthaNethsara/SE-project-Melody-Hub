package com.example.project1.pattern.state;

/** Final state: no further changes. */
public class DeliveredState extends AbstractOrderState {
    @Override public String name() { return "DELIVERED"; }
}
