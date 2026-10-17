package com.example.project1.service;

import com.example.project1.dto.WarrantyRequest;
import com.example.project1.dto.WarrantyResponse;
import com.example.project1.entity.Product;
import com.example.project1.entity.Warranty;
import com.example.project1.enums.WarrantyStatus;
import com.example.project1.repository.ProductRepository;
import com.example.project1.repository.WarrantyRepository;

import com.example.project1.repository.WarrantyClaimRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WarrantyServiceImpl implements WarrantyService {

    private final WarrantyRepository warrantyRepository;
    private final WarrantyClaimRepository warrantyClaimRepository;

    private final ProductRepository productRepository;

    public WarrantyServiceImpl(
            WarrantyRepository warrantyRepository,
            ProductRepository productRepository,
            WarrantyClaimRepository warrantyClaimRepository
    ) {
        this.warrantyClaimRepository = warrantyClaimRepository;
        this.warrantyRepository = warrantyRepository;
        this.productRepository = productRepository;
    }

    // =========================
    // CREATE WARRANTY
    // =========================

    @Override
    public WarrantyResponse createWarranty(
            WarrantyRequest request
    ) {

        // Check Product ID
        if (request.getProductId() == null) {

            throw new RuntimeException(
                    "Product ID is required"
            );
        }

        // Check warranty period
        if (request.getWarrantyPeriodMonths() == null ||
                request.getWarrantyPeriodMonths() <= 0 ||
                request.getWarrantyPeriodMonths() > 120) {

            throw new RuntimeException(
                    "Warranty period must be between 1 and 120 months"
            );
        }

        // Check warranty type
        if (request.getWarrantyType() == null) {

            throw new RuntimeException(
                    "Warranty type is required"
            );
        }

        // Find Product
        Product product = productRepository
                .findById(request.getProductId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Product not found"
                        )
                );

        // Check whether warranty already exists
        if (warrantyRepository
                .existsByProduct_ProductId(
                        request.getProductId()
                )) {

            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This product already has a warranty. Edit the existing warranty instead.");
        }

        // Create Warranty
        Warranty warranty = new Warranty();

        warranty.setProduct(product);

        warranty.setWarrantyPeriodMonths(
                request.getWarrantyPeriodMonths()
        );

        warranty.setWarrantyType(
                request.getWarrantyType()
        );

        warranty.setTermsAndConditions(
                request.getTermsAndConditions()
        );

        // Default status
        warranty.setStatus(
                request.getStatus() == null ? WarrantyStatus.ACTIVE : request.getStatus()
        );

        // Created date
        warranty.setCreatedDate(
                LocalDateTime.now()
        );

        // Save
        Warranty savedWarranty =
                warrantyRepository.save(warranty);

        return convertToResponse(savedWarranty);
    }


    // =========================
    // GET ALL WARRANTIES
    // =========================

    @Override
    public List<WarrantyResponse> getAllWarranties() {

        return warrantyRepository
                .findAll()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }


    // =========================
    // GET WARRANTY BY ID
    // =========================

    @Override
    public WarrantyResponse getWarrantyById(Long id) {

        Warranty warranty =
                warrantyRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Warranty not found"
                                )
                        );

        return convertToResponse(warranty);
    }


    // =========================
    // GET WARRANTY BY PRODUCT
    // =========================

    @Override
    public WarrantyResponse findWarrantyByProduct(Long productId) {
        return warrantyRepository.findByProduct_ProductId(productId)
                .map(this::convertToResponse)
                .orElse(null);
    }

    @Override
    public WarrantyResponse getWarrantyByProduct(
            Long productId
    ) {

        Warranty warranty =
                warrantyRepository
                        .findByProduct_ProductId(productId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Warranty not found for this product"
                                )
                        );

        return convertToResponse(warranty);
    }


    // =========================
    // UPDATE WARRANTY
    // =========================

    @Override
    public WarrantyResponse updateWarranty(
            Long id,
            WarrantyRequest request
    ) {

        Warranty warranty =
                warrantyRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Warranty not found"
                                )
                        );

        // Validate period
        if (request.getWarrantyPeriodMonths() == null ||
                request.getWarrantyPeriodMonths() <= 0 ||
                request.getWarrantyPeriodMonths() > 120) {

            throw new RuntimeException(
                    "Warranty period must be between 1 and 120 months"
            );
        }

        // Validate type
        if (request.getWarrantyType() == null) {

            throw new RuntimeException(
                    "Warranty type is required"
            );
        }

        warranty.setWarrantyPeriodMonths(
                request.getWarrantyPeriodMonths()
        );

        warranty.setWarrantyType(
                request.getWarrantyType()
        );

        warranty.setTermsAndConditions(
                request.getTermsAndConditions()
        );

        if (request.getStatus() != null) {
            warranty.setStatus(request.getStatus());
        }

        Warranty updatedWarranty =
                warrantyRepository.save(warranty);

        return convertToResponse(updatedWarranty);
    }


    // =========================
    // DELETE WARRANTY
    // =========================

    @Override
    public String deleteWarranty(Long id) {

        Warranty warranty =
                warrantyRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Warranty not found"
                                )
                        );

        if (warrantyClaimRepository.existsByWarranty_WarrantyId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Customers have already made claims under this warranty, so it cannot be deleted. "
                            + "Set its status to INACTIVE instead.");
        }

        warrantyRepository.delete(warranty);

        return "Warranty deleted successfully";
    }


    // =========================
    // CONVERT ENTITY → RESPONSE
    // =========================

    private WarrantyResponse convertToResponse(
            Warranty warranty
    ) {

        return new WarrantyResponse(

                warranty.getWarrantyId(),

                warranty.getProduct().getProductId(),

                warranty.getProduct().getName(),

                warranty.getWarrantyPeriodMonths(),

                warranty.getWarrantyType(),

                warranty.getTermsAndConditions(),

                warranty.getStatus(),

                warranty.getCreatedDate()
        );
    }
}
