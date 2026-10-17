package com.example.project1.service;

import com.example.project1.entity.Product;
import com.example.project1.enums.StockStatus;

import java.util.Set;

/** Small shared helpers for "can this product be sold?" and stock status. */
public final class StockRules {

    private StockRules() {
    }

    // Product.status is free text in the original design. These values mean "do not sell".
    private static final Set<String> NOT_SELLABLE = Set.of(
            "INACTIVE", "DISCONTINUED", "UNAVAILABLE", "ARCHIVED", "DISABLED", "OUT_OF_STOCK");

    public static boolean isSellable(Product product) {
        String status = product.getStatus();
        return status == null || !NOT_SELLABLE.contains(status.trim().toUpperCase());
    }

    public static void requireSellable(Product product) {
        if (!isSellable(product)) {
            throw new IllegalStateException(
                    "'" + product.getName() + "' is currently unavailable");
        }
    }

    public static StockStatus statusFor(Integer quantity, Integer reorderLevel) {
        if (quantity == null || quantity <= 0) {
            return StockStatus.OUT_OF_STOCK;
        }
        if (reorderLevel != null && quantity <= reorderLevel) {
            return StockStatus.LOW_STOCK;
        }
        return StockStatus.IN_STOCK;
    }
}
