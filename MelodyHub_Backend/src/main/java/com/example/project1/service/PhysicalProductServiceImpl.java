package com.example.project1.service;

import com.example.project1.exception.ApiErrors;

import com.example.project1.dto.PhysicalProductRequest;
import com.example.project1.dto.PhysicalProductResponse;
import com.example.project1.entity.PhysicalProduct;
import com.example.project1.entity.Product;
import com.example.project1.repository.PhysicalProductRepository;
import com.example.project1.repository.ProductRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PhysicalProductServiceImpl
        implements PhysicalProductService {

    private final PhysicalProductRepository physicalProductRepository;
    private final ProductRepository productRepository;

    public PhysicalProductServiceImpl(
            PhysicalProductRepository physicalProductRepository,
            ProductRepository productRepository) {

        this.physicalProductRepository = physicalProductRepository;
        this.productRepository = productRepository;
    }

    @Override
    public PhysicalProductResponse createPhysicalProduct(
            PhysicalProductRequest request) {

        Product product = productRepository.findById(
                        ApiErrors.required(request.getProductId(), "Please choose a product."))
                .orElseThrow(() -> ApiErrors.notFound("The selected product was not found."));

        if (physicalProductRepository.existsById(request.getProductId())) {
            throw ApiErrors.conflict(
                    "Physical details already exist for this product. Edit them instead.");
        }
        validate(request);

        PhysicalProduct physicalProduct = new PhysicalProduct();

        physicalProduct.setProduct(product);
        physicalProduct.setWeight(request.getWeight());
        physicalProduct.setShoppingClass(
                request.getShoppingClass());

        PhysicalProduct saved =
                physicalProductRepository.save(physicalProduct);

        return convertToResponse(saved);
    }

    @Override
    public List<PhysicalProductResponse> getAllPhysicalProducts() {

        return physicalProductRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PhysicalProductResponse getPhysicalProductById(
            Long productId) {

        PhysicalProduct physicalProduct =
                physicalProductRepository.findById(ApiErrors.required(productId, "Product id is required."))
                        .orElseThrow(() -> ApiErrors.notFound("Physical details not found."));

        return convertToResponse(physicalProduct);
    }

    @Override
    public PhysicalProductResponse updatePhysicalProduct(
            Long productId,
            PhysicalProductRequest request) {

        PhysicalProduct physicalProduct =
                physicalProductRepository.findById(ApiErrors.required(productId, "Product id is required."))
                        .orElseThrow(() -> ApiErrors.notFound("Physical details not found."));

        validate(request);
        physicalProduct.setWeight(request.getWeight());
        physicalProduct.setShoppingClass(
                request.getShoppingClass());

        PhysicalProduct updated =
                physicalProductRepository.save(physicalProduct);

        return convertToResponse(updated);
    }

    @Override
    public String deletePhysicalProduct(Long productId) {

        if (!physicalProductRepository.existsById(ApiErrors.required(productId, "Product id is required."))) {
            throw ApiErrors.notFound("Physical details not found.");
        }

        physicalProductRepository.deleteById(productId);

        return "Physical product deleted successfully";
    }

    private static void validate(PhysicalProductRequest request) {
        if (request.getWeight() != null && (request.getWeight() < 0 || request.getWeight() > 100000)) {
            throw ApiErrors.badRequest("Weight must be between 0 and 100000.");
        }
        ApiErrors.optionalText(request.getShoppingClass(), "Shipping class", 100);
    }

    private PhysicalProductResponse convertToResponse(
            PhysicalProduct physicalProduct) {

        return new PhysicalProductResponse(
                physicalProduct.getProductId(),
                physicalProduct.getWeight(),
                physicalProduct.getShoppingClass()
        );
    }
}
