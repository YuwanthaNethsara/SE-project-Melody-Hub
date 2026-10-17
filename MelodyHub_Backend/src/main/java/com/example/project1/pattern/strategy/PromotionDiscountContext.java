package com.example.project1.pattern.strategy;

import com.example.project1.entity.Promotion;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Context role: selects the matching DiscountStrategy for a promotion and runs it.
 * Used by PricingService (cart / checkout prices) and PromotionServiceImpl (promotion screens),
 * so the customer always sees the same price the order will use.
 */
@Component
public class PromotionDiscountContext {

    private final List<DiscountStrategy> strategies;
    private final NoDiscountStrategy noDiscount;

    public PromotionDiscountContext(List<DiscountStrategy> strategies, NoDiscountStrategy noDiscount) {
        this.strategies = strategies.stream().filter(s -> s != noDiscount).toList();
        this.noDiscount = noDiscount;
    }

    public DiscountStrategy select(Promotion promotion) {
        for (DiscountStrategy strategy : strategies) {
            if (strategy.appliesTo(promotion)) {
                return strategy;
            }
        }
        return noDiscount;
    }

    public BigDecimal apply(BigDecimal originalPrice, Promotion promotion) {
        return select(promotion).discountedPrice(originalPrice, promotion);
    }
}
