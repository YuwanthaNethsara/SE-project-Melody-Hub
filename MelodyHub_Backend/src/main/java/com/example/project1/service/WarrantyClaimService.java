package com.example.project1.service;

import com.example.project1.dto.EligibleWarrantyResponse;
import com.example.project1.dto.WarrantyClaimRequest;
import com.example.project1.dto.WarrantyClaimResponse;
import com.example.project1.enums.WarrantyClaimStatus;

import java.util.List;

public interface WarrantyClaimService {

    WarrantyClaimResponse createClaim(
            WarrantyClaimRequest request,
            String userEmail
    );

    List<WarrantyClaimResponse> getAllClaims();

    WarrantyClaimResponse getClaimById(Long id);

    List<WarrantyClaimResponse> getClaimsByUser(
            Long userId
    );

    List<WarrantyClaimResponse> getClaimsByStatus(
            WarrantyClaimStatus status
    );

    WarrantyClaimResponse updateClaimStatus(
            Long id,
            WarrantyClaimStatus status,
            String resolution
    );

    /** Claims submitted by one customer. */
    List<WarrantyClaimResponse> getClaimsByUserEmail(String email);

    /** Purchased, paid items of this customer that carry a warranty. */
    List<EligibleWarrantyResponse> getEligibleItems(String email);

    String deleteClaim(Long id);

    WarrantyClaimResponse updateOwnClaim(Long id, WarrantyClaimRequest request, String userEmail);

    String deleteOwnClaim(Long id, String userEmail);
}
