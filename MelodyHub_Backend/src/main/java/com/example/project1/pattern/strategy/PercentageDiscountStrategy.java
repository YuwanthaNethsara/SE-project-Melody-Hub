package com.example.project1.pattern.strategy;

import com.example.project1.entity.Promotion;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** ConcreteStrategy: "X% off" - the original behaviour of the promotion module. */
@Component
public class PercentageDiscountStrategy implements DiscountStrategy {

    @Override
    public boolean appliesTo(Promotion promotion) {
        Double percentage = promotion.getDiscountPercentage();
        return percentage != null && percentage > 0;
    }

    @Override
    public BigDecimal discountedPrice(BigDecimal originalPrice, Promotion promotion) {
        double percentage = Math.min(promotion.getDiscountPercentage(), 100.0);
        BigDecimal factor = BigDecimal.valueOf(100.0 - percentage)
                .divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
        return originalPrice.multiply(factor).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String name() {
        return "PERCENTAGE";
    }
}
