package com.example.project1.pattern.factory;

import com.example.project1.enums.ProductType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Picks the ConcreteCreator for a ProductType. Spring injects every ProductCreator bean. */
@Component
public class ProductCreatorProvider {

    private final Map<ProductType, ProductCreator> creators = new EnumMap<>(ProductType.class);

    public ProductCreatorProvider(List<ProductCreator> productCreators) {
        for (ProductCreator creator : productCreators) {
            creators.put(creator.getProductType(), creator);
        }
    }

    /** A request without a type means the catalog default, PHYSICAL (same as before the pattern). */
    public ProductCreator forType(ProductType type) {
        ProductType wanted = type == null ? ProductType.PHYSICAL : type;
        ProductCreator creator = creators.get(wanted);
        if (creator == null) {
            throw new IllegalArgumentException("Unsupported product type: " + wanted);
        }
        return creator;
    }
}
