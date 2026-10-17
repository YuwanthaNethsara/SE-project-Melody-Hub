package com.example.project1.pattern.factory;

import com.example.project1.dto.ProductRequest;
import com.example.project1.entity.Category;
import com.example.project1.entity.Product;
import com.example.project1.enums.ProductType;

/**
 * FACTORY METHOD pattern (Creational) - Product Catalog Management.
 *
 * Creator role: {@link #createProduct()} is the FACTORY METHOD. Each concrete creator decides
 * which kind of Product it builds and what is special about it, while {@link #create} /
 * {@link #populate} hold the steps every product shares. ProductServiceImpl never writes
 * "new Product()" itself - it asks the creator that matches the requested ProductType.
 */
public abstract class ProductCreator {

    /** The ProductType this creator is responsible for. */
    public abstract ProductType getProductType();

    /** FACTORY METHOD - subclasses return the right Product with its type-specific defaults. */
    protected abstract Product createProduct();

    /** Builds a brand-new product from a request (used when a product is added). */
    public Product create(ProductRequest request, Category category) {
        Product product = createProduct();
        populate(product, request, category);
        return product;
    }

    /** Copies the request onto a product (used for both create and update, so rules stay in one place). */
    public void populate(Product product, ProductRequest request, Category category) {
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setProductType(getProductType());
        product.setStatus(request.getStatus() == null || request.getStatus().isBlank()
                ? defaultStatus()
                : request.getStatus().trim().toUpperCase());
        product.setImageUrl(request.getImageUrl());
        product.setSku(request.getSku() == null || request.getSku().isBlank()
                ? null
                : request.getSku().trim().toUpperCase());
        product.setCategory(category);
    }

    protected String defaultStatus() {
        return "ACTIVE";
    }
}
