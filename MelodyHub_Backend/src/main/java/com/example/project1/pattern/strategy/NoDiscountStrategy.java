package com.example.project1.pattern.strategy;

import com.example.project1.entity.Promotion;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** ConcreteStrategy: the promotion carries no usable discount, so the price stays as it is. */
@Component
public class NoDiscountStrategy implements DiscountStrategy {

    @Override
    public boolean appliesTo(Promotion promotion) {
        return true; // fallback - the resolver asks the other strategies first
    }

    @Override
    public BigDecimal discountedPrice(BigDecimal originalPrice, Promotion promotion) {
        return originalPrice.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String name() {
        return "NONE";
    }
}
