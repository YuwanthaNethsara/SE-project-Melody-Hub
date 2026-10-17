package com.example.project1.service;

import com.example.project1.dto.PhysicalProductRequest;
import com.example.project1.dto.PhysicalProductResponse;

import java.util.List;

public interface PhysicalProductService {

    PhysicalProductResponse createPhysicalProduct(
            PhysicalProductRequest request);

    List<PhysicalProductResponse> getAllPhysicalProducts();

    PhysicalProductResponse getPhysicalProductById(Long productId);

    PhysicalProductResponse updatePhysicalProduct(
            Long productId,
            PhysicalProductRequest request);

    String deletePhysicalProduct(Long productId);
}
