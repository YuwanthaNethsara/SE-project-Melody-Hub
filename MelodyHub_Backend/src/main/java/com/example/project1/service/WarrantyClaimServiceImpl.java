package com.example.project1.service;

import com.example.project1.dto.EligibleWarrantyResponse;
import com.example.project1.dto.WarrantyClaimRequest;
import com.example.project1.dto.WarrantyClaimResponse;
import com.example.project1.entity.Order;
import com.example.project1.entity.OrderItem;
import com.example.project1.entity.User;
import com.example.project1.entity.Warranty;
import com.example.project1.entity.WarrantyClaim;
import com.example.project1.enums.WarrantyClaimStatus;
import com.example.project1.enums.WarrantyStatus;
import com.example.project1.repository.OrderItemRepository;
import com.example.project1.repository.UserRepository;
import com.example.project1.pattern.chain.ClaimValidationChain;
import com.example.project1.pattern.chain.WarrantyClaimContext;
import com.example.project1.repository.WarrantyClaimRepository;
import com.example.project1.repository.WarrantyRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WarrantyClaimServiceImpl implements WarrantyClaimService {

    private final WarrantyClaimRepository claimRepository;
    private final OrderItemRepository orderItemRepository;
    private final WarrantyRepository warrantyRepository;
    private final UserRepository userRepository;

    // Chain of Responsibility: validates a new claim step by step
    private final ClaimValidationChain claimValidationChain;

    public WarrantyClaimServiceImpl(
            WarrantyClaimRepository claimRepository,
            OrderItemRepository orderItemRepository,
            WarrantyRepository warrantyRepository,
            UserRepository userRepository,
            ClaimValidationChain claimValidationChain
    ) {
        this.claimValidationChain = claimValidationChain;
        this.claimRepository = claimRepository;
        this.orderItemRepository = orderItemRepository;
        this.warrantyRepository = warrantyRepository;
        this.userRepository = userRepository;
    }

    // ==========================================
    // CREATE WARRANTY CLAIM
    // ==========================================

    @Override
    public WarrantyClaimResponse createClaim(
            WarrantyClaimRequest request,
            String userEmail
    ) {

        // Check Order Item ID
        if (request.getOrderItemId() == null) {
            throw new RuntimeException(
                    "Order Item ID is required"
            );
        }

        // Check Claim Type
        if (request.getClaimType() == null) {
            throw new RuntimeException(
                    "Claim type is required"
            );
        }

        // Check Reason
        if (request.getReason() == null ||
                request.getReason().trim().isEmpty()) {

            throw new RuntimeException(
                    "Reason is required"
            );
        }

        checkReasonAndDescription(request);

        // ==========================================
        // FIND LOGGED-IN USER
        // ==========================================

        User user = userRepository
                .findByEmail(userEmail)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"
                        )
                );

        // ==========================================
        // CHAIN OF RESPONSIBILITY
        // Order -> Payment -> Warranty -> Eligibility.
        // Each handler rejects the claim or passes it on.
        // ==========================================

        WarrantyClaimContext context =
                new WarrantyClaimContext(request, user);

        claimValidationChain.validate(context);

        OrderItem orderItem = context.getOrderItem();
        Warranty warranty = context.getWarranty();

        // ==========================================
        // CREATE CLAIM
        // ==========================================

        WarrantyClaim claim = new WarrantyClaim();

        claim.setOrderItem(orderItem);

        claim.setWarranty(warranty);

        claim.setUser(user);

        claim.setClaimType(
                request.getClaimType()
        );

        claim.setReason(
                request.getReason().trim()
        );

        claim.setDescription(
                request.getDescription().trim()
        );

        claim.setClaimDate(
                LocalDateTime.now()
        );

        // Default status
        claim.setStatus(
                WarrantyClaimStatus.PENDING
        );

        claim.setResolution(null);

        // Save
        WarrantyClaim savedClaim =
                claimRepository.save(claim);

        return convertToResponse(savedClaim);
    }


    // ==========================================
    // GET ALL CLAIMS
    // ==========================================

    @Override
    public List<WarrantyClaimResponse> getAllClaims() {

        return claimRepository
                .findAll()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }


    // ==========================================
    // GET CLAIM BY ID
    // ==========================================

    @Override
    public WarrantyClaimResponse getClaimById(Long id) {

        WarrantyClaim claim =
                claimRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Warranty claim not found"
                                )
                        );

        return convertToResponse(claim);
    }


    // ==========================================
    // GET CLAIMS BY USER
    // ==========================================

    @Override
    public List<WarrantyClaimResponse> getClaimsByUser(
            Long userId
    ) {

        return claimRepository
                .findByUser_Id(userId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }


    // ==========================================
    // GET CLAIMS BY STATUS
    // ==========================================

    @Override
    public List<WarrantyClaimResponse> getClaimsByStatus(
            WarrantyClaimStatus status
    ) {

        return claimRepository
                .findByStatus(status)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }


    // ==========================================
    // UPDATE CLAIM STATUS
    // ==========================================

    @Override
    public WarrantyClaimResponse updateClaimStatus(
            Long id,
            WarrantyClaimStatus status,
            String resolution
    ) {

        WarrantyClaim claim =
                claimRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Warranty claim not found"
                                )
                        );

        if (status == null) {
            throw new RuntimeException(
                    "Status is required"
            );
        }

        WarrantyClaimStatus current = claim.getStatus();

        if (current != status) {

            boolean allowed =
                    (current == WarrantyClaimStatus.PENDING
                            && (status == WarrantyClaimStatus.APPROVED
                            || status == WarrantyClaimStatus.REJECTED))
                            || (current == WarrantyClaimStatus.APPROVED
                            && (status == WarrantyClaimStatus.COMPLETED
                            || status == WarrantyClaimStatus.REJECTED));

            if (!allowed) {
                throw new IllegalStateException(
                        "A " + current + " claim cannot be changed to " + status);
            }
        }

        if (status == WarrantyClaimStatus.REJECTED
                && (resolution == null || resolution.trim().isEmpty())) {

            throw new IllegalArgumentException(
                    "Please give a reason when rejecting a claim");
        }

        claim.setStatus(status);

        claim.setResolution(resolution);

        WarrantyClaim updatedClaim =
                claimRepository.save(claim);

        return convertToResponse(updatedClaim);
    }


    // ==========================================
    // DELETE CLAIM
    // ==========================================

    @Override
    public List<WarrantyClaimResponse> getClaimsByUserEmail(String email) {

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return claimRepository
                .findByUser_Id(user.getId())
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<EligibleWarrantyResponse> getEligibleItems(String email) {

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // claim status per order item (this customer's own claims only)
        java.util.Map<Long, WarrantyClaimStatus> claimByItem = new java.util.HashMap<>();
        for (WarrantyClaim c : claimRepository.findByUser_Id(user.getId())) {
            claimByItem.put(c.getOrderItem().getOrderItemId(), c.getStatus());
        }

        List<EligibleWarrantyResponse> result = new java.util.ArrayList<>();

        for (OrderItem item : orderItemRepository.findByOrder_User_Email(email)) {

            Order order = item.getOrder();

            // same rule as createClaim: only PAID orders count as purchased
            if (!"PAID".equalsIgnoreCase(order.getPaymentStatus())) {
                continue;
            }

            Warranty warranty = warrantyRepository
                    .findByProduct_ProductId(item.getProduct().getProductId())
                    .orElse(null);

            if (warranty == null) {
                continue;
            }

            EligibleWarrantyResponse dto = new EligibleWarrantyResponse();
            dto.setOrderItemId(item.getOrderItemId());
            dto.setOrderId(order.getOrderId());
            dto.setPurchaseDate(order.getOrderDate());
            dto.setProductId(item.getProduct().getProductId());
            dto.setProductName(item.getProduct().getName());
            dto.setQuantity(item.getQuantity());
            dto.setWarrantyId(warranty.getWarrantyId());
            dto.setWarrantyPeriodMonths(warranty.getWarrantyPeriodMonths());
            dto.setWarrantyType(warranty.getWarrantyType());
            dto.setTermsAndConditions(warranty.getTermsAndConditions());

            LocalDateTime expiry = null;
            if (order.getOrderDate() != null && warranty.getWarrantyPeriodMonths() != null) {
                expiry = order.getOrderDate().plusMonths(warranty.getWarrantyPeriodMonths());
                dto.setExpiresOn(expiry.toLocalDate());
            }

            WarrantyClaimStatus existing = claimByItem.get(item.getOrderItemId());
            dto.setClaimStatus(existing);

            if (existing != null) {
                dto.setClaimable(false);
                dto.setNotClaimableReason("A warranty claim has already been submitted for this item.");
            } else if (warranty.getStatus() != WarrantyStatus.ACTIVE) {
                dto.setClaimable(false);
                dto.setNotClaimableReason("This warranty is not active");
            } else if (expiry != null && LocalDateTime.now().isAfter(expiry)) {
                dto.setClaimable(false);
                dto.setNotClaimableReason("Warranty period has expired for this product.");
            } else {
                dto.setClaimable(true);
            }

            result.add(dto);
        }

        return result;
    }

    // ==========================================
    // CUSTOMER: edit own claim (only while it is still PENDING)
    // ==========================================

    @Override
    public WarrantyClaimResponse updateOwnClaim(
            Long id,
            WarrantyClaimRequest request,
            String userEmail
    ) {

        WarrantyClaim claim = findOwnPendingClaim(id, userEmail, "edited");

        if (request.getClaimType() == null) {
            throw new IllegalArgumentException("Claim type is required");
        }

        checkReasonAndDescription(request);

        // only these three fields can change: never the order item, the owner or the status
        claim.setClaimType(request.getClaimType());
        claim.setReason(request.getReason().trim());
        claim.setDescription(request.getDescription().trim());

        return convertToResponse(claimRepository.save(claim));
    }

    // ==========================================
    // CUSTOMER: withdraw (delete) own claim while it is still PENDING
    // ==========================================

    @Override
    public String deleteOwnClaim(Long id, String userEmail) {

        WarrantyClaim claim = findOwnPendingClaim(id, userEmail, "withdrawn");

        claimRepository.delete(claim);

        return "Warranty claim withdrawn successfully";
    }

    private WarrantyClaim findOwnPendingClaim(Long id, String userEmail, String action) {

        WarrantyClaim claim = claimRepository
                .findById(id)
                .orElseThrow(() -> new RuntimeException("Warranty claim not found"));

        if (!claim.getUser().getEmail().equalsIgnoreCase(userEmail)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "You cannot access another user's warranty claim");
        }

        if (claim.getStatus() != WarrantyClaimStatus.PENDING) {
            throw new IllegalStateException(
                    "Only a PENDING claim can be " + action + ". This claim is already "
                            + claim.getStatus() + ".");
        }

        return claim;
    }

    private void checkReasonAndDescription(WarrantyClaimRequest request) {

        if (request.getReason() == null || request.getReason().trim().isEmpty()) {
            throw new IllegalArgumentException("Reason is required");
        }

        if (request.getReason().trim().length() > 1000) {
            throw new IllegalArgumentException("Reason must be at most 1000 characters");
        }

        if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Description is required");
        }

        if (request.getDescription().trim().length() > 2000) {
            throw new IllegalArgumentException("Description must be at most 2000 characters");
        }
    }

    @Override
    public String deleteClaim(Long id) {

        WarrantyClaim claim =
                claimRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Warranty claim not found"
                                )
                        );

        claimRepository.delete(claim);

        return "Warranty claim deleted successfully";
    }


    // ==========================================
    // CONVERT ENTITY TO RESPONSE
    // ==========================================

    private WarrantyClaimResponse convertToResponse(
            WarrantyClaim claim
    ) {

        OrderItem orderItem =
                claim.getOrderItem();

        return new WarrantyClaimResponse(

                claim.getClaimId(),

                orderItem.getOrderItemId(),

                orderItem.getOrder().getOrderId(),

                orderItem.getProduct().getProductId(),

                orderItem.getProduct().getName(),

                claim.getWarranty().getWarrantyId(),

                claim.getUser().getId(),

                claim.getUser().getName(),

                claim.getUser().getEmail(),

                claim.getClaimType(),

                claim.getReason(),

                claim.getDescription(),

                claim.getClaimDate(),

                claim.getStatus(),

                claim.getResolution()
        );
    }
}