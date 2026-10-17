package com.example.project1.dto;

/** Body of PUT /api/orders/{id}/shipping-address. */
public class ShippingAddressRequest {

    private String shippingAddress;

    public ShippingAddressRequest() {
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }
}
