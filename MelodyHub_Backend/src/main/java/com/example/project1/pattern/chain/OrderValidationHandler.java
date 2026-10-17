package com.example.project1.pattern.chain;

import com.example.project1.entity.OrderItem;
import com.example.project1.repository.OrderItemRepository;
import org.springframework.stereotype.Component;

/** Link 1: the order item exists and was bought by the customer who is claiming. */
@Component
public class OrderValidationHandler extends ClaimValidationHandler {

    private final OrderItemRepository orderItemRepository;

    public OrderValidationHandler(OrderItemRepository orderItemRepository) {
        this.orderItemRepository = orderItemRepository;
    }

    @Override
    protected void check(WarrantyClaimContext claim) {
        OrderItem orderItem = orderItemRepository
                .findById(claim.getRequest().getOrderItemId())
                .orElseThrow(() -> new RuntimeException("Order item not found"));
        claim.setOrderItem(orderItem);

        if (!claim.getOrder().getUser().getId().equals(claim.getUser().getId())) {
            throw new RuntimeException("You can only claim warranty for your own purchased items");
        }
    }
}
