package com.example.project1.pattern.chain;

import org.springframework.stereotype.Component;

/** Link 2: only PAID orders count as purchased. */
@Component
public class PaymentValidationHandler extends ClaimValidationHandler {

    @Override
    protected void check(WarrantyClaimContext claim) {
        if (!"PAID".equalsIgnoreCase(claim.getOrder().getPaymentStatus())) {
            throw new RuntimeException("Warranty claim is allowed only for paid orders");
        }
    }
}
