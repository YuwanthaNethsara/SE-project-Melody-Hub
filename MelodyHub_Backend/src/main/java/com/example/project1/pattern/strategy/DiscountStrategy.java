package com.example.project1.pattern.strategy;

import com.example.project1.entity.Promotion;

import java.math.BigDecimal;

/**
 * STRATEGY pattern (Behavioral) - Marketing & Promotion Management.
 *
 * Strategy role: one interchangeable algorithm for "what does the customer pay once this
 * promotion is applied". New discount kinds are added as new strategies without touching
 * PricingService or PromotionServiceImpl (no big if/else).
 */
public interface DiscountStrategy {

    /** Does this strategy know how to calculate the given promotion? */
    boolean appliesTo(Promotion promotion);

    /** Price for ONE unit after the promotion. Never below zero, always 2 decimals. */
    BigDecimal discountedPrice(BigDecimal originalPrice, Promotion promotion);

    /** Short name used in logs / reports, e.g. PERCENTAGE. */
    String name();
}
