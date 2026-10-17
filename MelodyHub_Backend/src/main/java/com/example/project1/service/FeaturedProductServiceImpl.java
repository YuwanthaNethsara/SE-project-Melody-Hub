package com.example.project1.service;

import com.example.project1.dto.FeaturedProductRequest;
import com.example.project1.dto.FeaturedProductResponse;
import com.example.project1.entity.FeaturedProduct;
import com.example.project1.entity.Product;
import com.example.project1.repository.FeaturedProductRepository;
import com.example.project1.repository.ProductRepository;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FeaturedProductServiceImpl
        implements FeaturedProductService {

    private final FeaturedProductRepository
            featuredProductRepository;

    private final ProductRepository productRepository;

    public FeaturedProductServiceImpl(
            FeaturedProductRepository featuredProductRepository,
            ProductRepository productRepository
    ) {
        this.featuredProductRepository = featuredProductRepository;
        this.productRepository = productRepository;
    }

    // ADD
    @Override
    public FeaturedProductResponse addFeaturedProduct(
            FeaturedProductRequest request
    ) {

        if (request.getProductId() == null) {
            throw new RuntimeException(
                    "Product ID is required"
            );
        }

        Product product = productRepository
                .findById(request.getProductId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Product not found"
                        )
                );

        if (featuredProductRepository
                .existsByProduct_ProductId(
                        request.getProductId()
                )) {

            throw new RuntimeException(
                    "Product is already featured"
            );
        }

        FeaturedProduct featuredProduct =
                new FeaturedProduct();

        featuredProduct.setProduct(product);

        featuredProduct.setStatus("ACTIVE");

        featuredProduct.setFeaturedDate(
                LocalDateTime.now()
        );

        FeaturedProduct saved =
                featuredProductRepository.save(
                        featuredProduct
                );

        return convertToResponse(saved);
    }

    // GET ALL
    @Override
    public List<FeaturedProductResponse>
    getAllFeaturedProducts() {

        return featuredProductRepository
                .findAll()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // GET ACTIVE
    @Override
    public List<FeaturedProductResponse>
    getActiveFeaturedProducts() {

        return featuredProductRepository
                .findByStatus("ACTIVE")
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // GET BY ID
    @Override
    public FeaturedProductResponse
    getFeaturedProductById(Long id) {

        FeaturedProduct featuredProduct =
                featuredProductRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Featured product not found"
                                )
                        );

        return convertToResponse(featuredProduct);
    }

    // UPDATE
    @Override
    public FeaturedProductResponse
    updateFeaturedProduct(
            Long id,
            FeaturedProductRequest request
    ) {

        FeaturedProduct featuredProduct =
                featuredProductRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Featured product not found"
                                )
                        );

        Product product = productRepository
                .findById(request.getProductId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Product not found"
                        )
                );

        featuredProduct.setProduct(product);

        FeaturedProduct updated =
                featuredProductRepository.save(
                        featuredProduct
                );

        return convertToResponse(updated);
    }

    // REMOVE
    @Override
    public String removeFeaturedProduct(Long id) {

        if (!featuredProductRepository.existsById(id)) {

            throw new RuntimeException(
                    "Featured product not found"
            );
        }

        featuredProductRepository.deleteById(id);

        return "Featured product removed successfully";
    }

    // CONVERT
    private FeaturedProductResponse
    convertToResponse(
            FeaturedProduct featuredProduct
    ) {

        Product product =
                featuredProduct.getProduct();

        Double price =
                product.getPrice().doubleValue();

        return new FeaturedProductResponse(

                featuredProduct.getFeaturedProductId(),

                product.getProductId(),

                product.getName(),

                price,

                product.getImageUrl(),

                featuredProduct.getStatus(),

                featuredProduct.getFeaturedDate()
        );
    }
}
