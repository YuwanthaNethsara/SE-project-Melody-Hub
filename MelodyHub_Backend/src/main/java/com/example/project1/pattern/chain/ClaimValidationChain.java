package com.example.project1.pattern.chain;

import org.springframework.stereotype.Component;

/**
 * Builds the chain once:  Order -> Payment -> Warranty -> Eligibility -> (claim is processed).
 * WarrantyClaimServiceImpl calls validate() before it saves a claim.
 */
@Component
public class ClaimValidationChain {

    private final ClaimValidationHandler head;

    public ClaimValidationChain(OrderValidationHandler orderHandler,
                                PaymentValidationHandler paymentHandler,
                                WarrantyValidationHandler warrantyHandler,
                                EligibilityValidationHandler eligibilityHandler) {
        orderHandler.setNext(paymentHandler)
                .setNext(warrantyHandler)
                .setNext(eligibilityHandler);
        this.head = orderHandler;
    }

    /** Throws the first handler's rejection, otherwise the claim may be processed. */
    public void validate(WarrantyClaimContext claim) {
        head.handle(claim);
    }
}
