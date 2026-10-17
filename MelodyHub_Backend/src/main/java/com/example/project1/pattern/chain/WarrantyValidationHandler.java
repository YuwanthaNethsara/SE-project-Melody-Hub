package com.example.project1.pattern.chain;

import com.example.project1.entity.Warranty;
import com.example.project1.enums.WarrantyStatus;
import com.example.project1.repository.WarrantyRepository;
import org.springframework.stereotype.Component;

/** Link 3: the product has a warranty and it is ACTIVE. */
@Component
public class WarrantyValidationHandler extends ClaimValidationHandler {

    private final WarrantyRepository warrantyRepository;

    public WarrantyValidationHandler(WarrantyRepository warrantyRepository) {
        this.warrantyRepository = warrantyRepository;
    }

    @Override
    protected void check(WarrantyClaimContext claim) {
        Long productId = claim.getOrderItem().getProduct().getProductId();

        Warranty warranty = warrantyRepository
                .findByProduct_ProductId(productId)
                .orElseThrow(() -> new RuntimeException("This product does not have a warranty"));

        if (warranty.getStatus() != WarrantyStatus.ACTIVE) {
            throw new RuntimeException("Warranty is not active");
        }
        claim.setWarranty(warranty);
    }
}
