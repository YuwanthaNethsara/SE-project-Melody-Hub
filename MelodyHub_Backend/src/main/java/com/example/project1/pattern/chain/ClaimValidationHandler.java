package com.example.project1.pattern.chain;

/**
 * CHAIN OF RESPONSIBILITY pattern (Behavioral) - Warranty & Return Management.
 *
 * Handler role. A warranty claim must pass four checks. Each handler owns exactly ONE check and
 * either rejects the claim (throws) or passes it on to the next handler. The service only talks
 * to the head of the chain; adding, removing or re-ordering a rule never touches the others.
 */
public abstract class ClaimValidationHandler {

    private ClaimValidationHandler next;

    public ClaimValidationHandler setNext(ClaimValidationHandler next) {
        this.next = next;
        return next; // lets the chain be wired fluently: a.setNext(b).setNext(c)
    }

    /** Runs this handler's rule, then hands the claim to the next handler. */
    public final void handle(WarrantyClaimContext claim) {
        check(claim);               // reject by throwing...
        if (next != null) {
            next.handle(claim);     // ...or pass it on
        }
    }

    /** The single rule of this handler. */
    protected abstract void check(WarrantyClaimContext claim);
}
