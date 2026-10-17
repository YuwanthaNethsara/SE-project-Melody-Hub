package com.example.project1.pattern.strategy;

import com.example.project1.entity.Promotion;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** ConcreteStrategy: "LKR/USD X off each unit" - a fixed amount taken from the unit price. */
@Component
public class FixedAmountDiscountStrategy implements DiscountStrategy {

    @Override
    public boolean appliesTo(Promotion promotion) {
        Double amount = promotion.getDiscountAmount();
        return amount != null && amount > 0
                && (promotion.getDiscountPercentage() == null || promotion.getDiscountPercentage() <= 0);
    }

    @Override
    public BigDecimal discountedPrice(BigDecimal originalPrice, Promotion promotion) {
        BigDecimal discounted = originalPrice.subtract(BigDecimal.valueOf(promotion.getDiscountAmount()));
        if (discounted.signum() < 0) {
            discounted = BigDecimal.ZERO;
        }
        return discounted.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String name() {
        return "FIXED_AMOUNT";
    }
}
