package com.example.project1.service;

import com.example.project1.dto.FeaturedProductRequest;
import com.example.project1.dto.FeaturedProductResponse;

import java.util.List;

public interface FeaturedProductService {

    FeaturedProductResponse addFeaturedProduct(
            FeaturedProductRequest request
    );

    List<FeaturedProductResponse> getAllFeaturedProducts();

    List<FeaturedProductResponse> getActiveFeaturedProducts();

    FeaturedProductResponse getFeaturedProductById(Long id);

    FeaturedProductResponse updateFeaturedProduct(
            Long id,
            FeaturedProductRequest request
    );

    String removeFeaturedProduct(Long id);
}
