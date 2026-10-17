package com.example.project1.service;

import com.example.project1.dto.PromotionRequest;
import com.example.project1.dto.PromotionResponse;
import com.example.project1.entity.Product;
import com.example.project1.entity.Promotion;
import com.example.project1.pattern.strategy.DiscountStrategy;
import com.example.project1.pattern.strategy.PromotionDiscountContext;
import com.example.project1.repository.ProductRepository;
import com.example.project1.repository.PromotionRepository;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PromotionServiceImpl
        implements PromotionService {

    private final PromotionRepository promotionRepository;

    private final ProductRepository productRepository;

    // Strategy pattern: picks the discount algorithm that matches each promotion
    private final PromotionDiscountContext discountContext;

    public PromotionServiceImpl(
            PromotionRepository promotionRepository,
            ProductRepository productRepository,
            PromotionDiscountContext discountContext
    ) {
        this.promotionRepository = promotionRepository;
        this.productRepository = productRepository;
        this.discountContext = discountContext;
    }

    // ==========================================
    // CREATE PROMOTION
    // ==========================================

    @Override
    public PromotionResponse createPromotion(
            PromotionRequest request
    ) {

        validateRequest(request);

        Product product = productRepository
                .findById(request.getProductId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Product not found"
                        )
                );

        Promotion promotion = new Promotion();

        promotion.setPromotionName(
                request.getPromotionName()
        );

        promotion.setDescription(
                request.getDescription()
        );

        promotion.setProduct(product);

        applyDiscount(promotion, request, product);

        promotion.setStartDate(
                request.getStartDate()
        );

        promotion.setEndDate(
                request.getEndDate()
        );

        promotion.setStatus(
                calculateStatus(
                        request.getStartDate(),
                        request.getEndDate()
                )
        );

        promotion.setCreatedDate(
                LocalDateTime.now()
        );

        Promotion savedPromotion =
                promotionRepository.save(promotion);

        return convertToResponse(savedPromotion);
    }

    // ==========================================
    // GET ALL PROMOTIONS
    // ==========================================

    @Override
    public List<PromotionResponse> getAllPromotions() {

        updateExpiredPromotions();

        return promotionRepository
                .findAll()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // ==========================================
    // GET ACTIVE PROMOTIONS
    // ==========================================

    @Override
    public List<PromotionResponse> getActivePromotions() {

        updateExpiredPromotions();

        return promotionRepository
                .findByStatus("ACTIVE")
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // ==========================================
    // GET PROMOTION BY ID
    // ==========================================

    @Override
    public PromotionResponse getPromotionById(
            Long id
    ) {

        Promotion promotion =
                promotionRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Promotion not found"
                                )
                        );

        updateStatus(promotion);

        return convertToResponse(promotion);
    }

    // ==========================================
    // GET BY PRODUCT
    // ==========================================

    @Override
    public List<PromotionResponse> getPromotionsByProduct(
            Long productId
    ) {

        return promotionRepository
                .findByProduct_ProductId(productId)
                .stream()
                .map(promotion -> {

                    updateStatus(promotion);

                    return convertToResponse(promotion);
                })
                .collect(Collectors.toList());
    }

    // ==========================================
    // SEARCH
    // ==========================================

    @Override
    public List<PromotionResponse> searchPromotions(
            String name
    ) {

        return promotionRepository
                .findByPromotionNameContainingIgnoreCase(name)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // ==========================================
    // UPDATE
    // ==========================================

    @Override
    public PromotionResponse updatePromotion(
            Long id,
            PromotionRequest request
    ) {

        validateRequest(request);

        Promotion promotion =
                promotionRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Promotion not found"
                                )
                        );

        Product product =
                productRepository
                        .findById(request.getProductId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Product not found"
                                )
                        );

        promotion.setPromotionName(
                request.getPromotionName()
        );

        promotion.setDescription(
                request.getDescription()
        );

        promotion.setProduct(product);

        applyDiscount(promotion, request, product);

        promotion.setStartDate(
                request.getStartDate()
        );

        promotion.setEndDate(
                request.getEndDate()
        );

        promotion.setStatus(
                calculateStatus(
                        request.getStartDate(),
                        request.getEndDate()
                )
        );

        Promotion updatedPromotion =
                promotionRepository.save(promotion);

        return convertToResponse(updatedPromotion);
    }

    // ==========================================
    // DELETE
    // ==========================================

    @Override
    public String deletePromotion(Long id) {

        if (!promotionRepository.existsById(id)) {

            throw new RuntimeException(
                    "Promotion not found"
            );
        }

        promotionRepository.deleteById(id);

        return "Promotion deleted successfully";
    }

    // ==========================================
    // DISCOUNT TYPE (percentage OR fixed amount)
    // ==========================================

    private void applyDiscount(
            Promotion promotion,
            PromotionRequest request,
            Product product
    ) {

        boolean fixed = request.getDiscountAmount() != null
                && request.getDiscountAmount() > 0;

        if (fixed) {

            if (product.getPrice() != null
                    && request.getDiscountAmount() >= product.getPrice().doubleValue()) {

                throw new RuntimeException(
                        "Fixed discount amount must be less than the product price"
                );
            }

            promotion.setDiscountAmount(request.getDiscountAmount());
            promotion.setDiscountPercentage(0.0);

        } else {

            promotion.setDiscountAmount(null);
            promotion.setDiscountPercentage(
                    request.getDiscountPercentage()
            );
        }
    }

    // ==========================================
    // VALIDATION
    // ==========================================

    private void validateRequest(
            PromotionRequest request
    ) {

        if (request.getPromotionName() == null ||
                request.getPromotionName().trim().isEmpty()) {

            throw new RuntimeException(
                    "Promotion name is required"
            );
        }

        if (request.getProductId() == null) {

            throw new RuntimeException(
                    "Product ID is required"
            );
        }

        boolean hasPercentage = request.getDiscountPercentage() != null
                && request.getDiscountPercentage() > 0;
        boolean hasAmount = request.getDiscountAmount() != null
                && request.getDiscountAmount() > 0;

        if (hasPercentage && hasAmount) {

            throw new RuntimeException(
                    "Choose either a discount percentage or a fixed discount amount, not both"
            );
        }

        if (!hasAmount && (request.getDiscountPercentage() == null ||
                request.getDiscountPercentage() <= 0 ||
                request.getDiscountPercentage() > 100)) {

            throw new RuntimeException(
                    "Discount percentage must be between 0 and 100"
            );
        }

        if (hasAmount && request.getDiscountPercentage() != null
                && request.getDiscountPercentage() != 0) {

            throw new RuntimeException(
                    "Choose either a discount percentage or a fixed discount amount, not both"
            );
        }

        if (request.getStartDate() == null) {

            throw new RuntimeException(
                    "Start date is required"
            );
        }

        if (request.getEndDate() == null) {

            throw new RuntimeException(
                    "End date is required"
            );
        }

        if (request.getEndDate()
                .isBefore(request.getStartDate())) {

            throw new RuntimeException(
                    "End date cannot be before start date"
            );
        }
    }

    // ==========================================
    // CALCULATE STATUS
    // ==========================================

    private String calculateStatus(
            LocalDate startDate,
            LocalDate endDate
    ) {

        LocalDate today = LocalDate.now();

        if (today.isBefore(startDate)) {

            return "UPCOMING";
        }

        if (today.isAfter(endDate)) {

            return "EXPIRED";
        }

        return "ACTIVE";
    }

    // ==========================================
    // UPDATE EXPIRED PROMOTIONS
    // ==========================================

    private void updateExpiredPromotions() {

        List<Promotion> promotions =
                promotionRepository.findAll();

        for (Promotion promotion : promotions) {

            updateStatus(promotion);
        }
    }

    private void updateStatus(
            Promotion promotion
    ) {

        String currentStatus =
                calculateStatus(
                        promotion.getStartDate(),
                        promotion.getEndDate()
                );

        if (!currentStatus.equals(
                promotion.getStatus())) {

            promotion.setStatus(currentStatus);

            promotionRepository.save(promotion);
        }
    }

    // ==========================================
    // ENTITY → RESPONSE
    // ==========================================

    private PromotionResponse convertToResponse(
            Promotion promotion
    ) {

        Product product =
                promotion.getProduct();

        double originalPrice =
                product.getPrice().doubleValue();

        double discount =
                promotion.getDiscountPercentage() == null
                        ? 0.0
                        : promotion.getDiscountPercentage();

        // STRATEGY pattern: the matching DiscountStrategy calculates the discounted price
        DiscountStrategy strategy =
                discountContext.select(promotion);

        double discountedPrice =
                strategy.discountedPrice(
                        product.getPrice(),
                        promotion
                ).doubleValue();

        PromotionResponse response = new PromotionResponse(

                promotion.getPromotionId(),

                promotion.getPromotionName(),

                promotion.getDescription(),

                product.getProductId(),

                product.getName(),

                originalPrice,

                discount,

                discountedPrice,

                promotion.getStartDate(),

                promotion.getEndDate(),

                promotion.getStatus(),

                promotion.getCreatedDate()
        );

        response.setDiscountAmount(promotion.getDiscountAmount());
        response.setDiscountType(strategy.name());

        return response;
    }
}
