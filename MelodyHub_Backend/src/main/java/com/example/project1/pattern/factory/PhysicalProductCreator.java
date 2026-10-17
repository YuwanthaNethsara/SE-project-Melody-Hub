package com.example.project1.pattern.factory;

import com.example.project1.entity.Product;
import com.example.project1.enums.ProductType;
import org.springframework.stereotype.Component;

/**
 * ConcreteCreator: creates PHYSICAL catalog products (instruments, accessories, ...).
 * To support another product type later: add the enum value and ONE more creator like this one;
 * ProductServiceImpl and the controller do not change.
 */
@Component
public class PhysicalProductCreator extends ProductCreator {

    @Override
    public ProductType getProductType() {
        return ProductType.PHYSICAL;
    }

    @Override
    protected Product createProduct() {
        Product product = new Product();
        product.setProductType(ProductType.PHYSICAL);
        return product;
    }
}
