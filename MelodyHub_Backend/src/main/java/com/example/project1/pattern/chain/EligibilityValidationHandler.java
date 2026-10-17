package com.example.project1.pattern.chain;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import com.example.project1.entity.Order;
import com.example.project1.entity.Warranty;
import com.example.project1.repository.WarrantyClaimRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/** Link 4: the warranty period (counted from the purchase date) has not ended and no claim exists yet. */
@Component
public class EligibilityValidationHandler extends ClaimValidationHandler {

    private final WarrantyClaimRepository claimRepository;

    public EligibilityValidationHandler(WarrantyClaimRepository claimRepository) {
        this.claimRepository = claimRepository;
    }

    @Override
    protected void check(WarrantyClaimContext claim) {
        Order order = claim.getOrder();
        Warranty warranty = claim.getWarranty();

        if (order.getOrderDate() != null
                && warranty.getWarrantyPeriodMonths() != null
                && LocalDateTime.now().isAfter(
                order.getOrderDate().plusMonths(warranty.getWarrantyPeriodMonths()))) {
            throw new IllegalStateException("Warranty period has expired for this product.");
        }

        if (claimRepository.existsByOrderItem_OrderItemId(claim.getRequest().getOrderItemId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A warranty claim has already been submitted for this item.");
        }
    }
}
