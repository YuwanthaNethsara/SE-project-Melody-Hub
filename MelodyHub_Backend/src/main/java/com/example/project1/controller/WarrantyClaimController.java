package com.example.project1.controller;

import com.example.project1.dto.EligibleWarrantyResponse;
import com.example.project1.dto.WarrantyClaimRequest;
import com.example.project1.dto.WarrantyClaimResponse;
import com.example.project1.entity.User;
import com.example.project1.enums.WarrantyClaimStatus;
import com.example.project1.repository.UserRepository;
import com.example.project1.service.WarrantyClaimService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * CUSTOMER      - submit a claim for something they bought; see only their own claims
 * SALES_MANAGER - see all claims and decide them (approve / reject / complete)
 * ADMIN         - same as SALES_MANAGER, plus delete
 * (role checks are in SecurityConfig, ownership checks are here)
 */
@RestController
@RequestMapping("/api/warranty-claims")
public class WarrantyClaimController {

    private final WarrantyClaimService claimService;
    private final UserRepository userRepository;

    public WarrantyClaimController(
            WarrantyClaimService claimService,
            UserRepository userRepository
    ) {
        this.claimService = claimService;
        this.userRepository = userRepository;
    }

    // CUSTOMER - the claimant is always the logged-in user (never taken from the request)
    @PostMapping
    public ResponseEntity<WarrantyClaimResponse> createClaim(
            @RequestBody WarrantyClaimRequest request,
            Authentication authentication
    ) {
        WarrantyClaimResponse response =
                claimService.createClaim(request, authentication.getName());

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // CUSTOMER - edit own PENDING claim (type, reason, description only)
    @PutMapping("/{id}")
    public ResponseEntity<WarrantyClaimResponse> updateOwnClaim(
            @PathVariable Long id,
            @RequestBody WarrantyClaimRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                claimService.updateOwnClaim(id, request, authentication.getName()));
    }

    // CUSTOMER - withdraw own PENDING claim
    @DeleteMapping("/my/{id}")
    public ResponseEntity<String> deleteOwnClaim(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                claimService.deleteOwnClaim(id, authentication.getName()));
    }

    // CUSTOMER - my claims
    @GetMapping("/my")
    public ResponseEntity<List<WarrantyClaimResponse>> getMyClaims(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                claimService.getClaimsByUserEmail(authentication.getName()));
    }

    // CUSTOMER - purchased items that have a warranty (and whether a claim is possible)
    @GetMapping("/eligible")
    public ResponseEntity<List<EligibleWarrantyResponse>> getEligible(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                claimService.getEligibleItems(authentication.getName()));
    }

    // ADMIN / SALES_MANAGER
    @GetMapping
    public ResponseEntity<List<WarrantyClaimResponse>> getAllClaims() {
        return ResponseEntity.ok(claimService.getAllClaims());
    }

    @GetMapping("/{id}")
    public ResponseEntity<WarrantyClaimResponse> getClaimById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        WarrantyClaimResponse claim = claimService.getClaimById(id);

        if (!isStaff(authentication)
                && !claim.getUserEmail().equalsIgnoreCase(authentication.getName())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "You cannot access another user's warranty claim");
        }

        return ResponseEntity.ok(claim);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<WarrantyClaimResponse>> getClaimsByUser(
            @PathVariable Long userId,
            Authentication authentication
    ) {
        if (!isStaff(authentication)) {
            User me = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!me.getId().equals(userId)) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "You can only view your own warranty claims");
            }
        }

        return ResponseEntity.ok(claimService.getClaimsByUser(userId));
    }

    // ADMIN / SALES_MANAGER
    @GetMapping("/status/{status}")
    public ResponseEntity<List<WarrantyClaimResponse>> getClaimsByStatus(
            @PathVariable WarrantyClaimStatus status
    ) {
        return ResponseEntity.ok(claimService.getClaimsByStatus(status));
    }

    // ADMIN / SALES_MANAGER - customers can NOT change the status of a claim
    @PutMapping("/{id}/status")
    public ResponseEntity<WarrantyClaimResponse> updateClaimStatus(
            @PathVariable Long id,
            @RequestParam WarrantyClaimStatus status,
            @RequestParam(required = false) String resolution
    ) {
        return ResponseEntity.ok(
                claimService.updateClaimStatus(id, status, resolution));
    }

    // ADMIN
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteClaim(@PathVariable Long id) {
        return ResponseEntity.ok(claimService.deleteClaim(id));
    }

    private boolean isStaff(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority())
                        || "ROLE_SALES_MANAGER".equals(a.getAuthority()));
    }
}
