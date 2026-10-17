package com.example.project1.service;

import com.example.project1.entity.Product;
import com.example.project1.entity.Promotion;
import com.example.project1.pattern.strategy.PromotionDiscountContext;
import com.example.project1.repository.PromotionRepository;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * The ONLY place that decides what a customer pays for one unit.
 * Prices always come from the database (never from the browser):
 * product price minus the best currently-running promotion, if any.
 */
@Service
public class PricingService {

    private final PromotionRepository promotionRepository;
    private final PromotionDiscountContext discountContext;

    public PricingService(PromotionRepository promotionRepository,
                          PromotionDiscountContext discountContext) {
        this.promotionRepository = promotionRepository;
        this.discountContext = discountContext;
    }

    public BigDecimal unitPrice(Product product) {

        BigDecimal price = product.getPrice();
        if (price == null) {
            throw new IllegalStateException("'" + product.getName() + "' has no price");
        }

        BigDecimal best = price.setScale(2, RoundingMode.HALF_UP);
        LocalDate today = LocalDate.now();

        for (Promotion promotion : promotionRepository.findByProduct_ProductId(product.getProductId())) {

            boolean started = promotion.getStartDate() == null || !today.isBefore(promotion.getStartDate());
            boolean notEnded = promotion.getEndDate() == null || !today.isAfter(promotion.getEndDate());
            boolean flagged = promotion.getStatus() == null
                    || "ACTIVE".equalsIgnoreCase(promotion.getStatus());

            if (started && notEnded && flagged) {
                // STRATEGY pattern: the promotion's own discount strategy calculates the price
                BigDecimal candidate = discountContext.apply(price, promotion);
                if (candidate.compareTo(best) < 0) {
                    best = candidate; // the customer always gets the best running promotion
                }
            }
        }

        return best;
    }

    public double lineTotal(BigDecimal unitPrice, int quantity) {
        return unitPrice.multiply(BigDecimal.valueOf(quantity))
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
