package com.example.project1.service;

import com.example.project1.dto.ReviewRequest;
import com.example.project1.dto.ReviewResponse;
import com.example.project1.dto.ReviewSummaryResponse;
import com.example.project1.dto.ReviewableProductResponse;
import com.example.project1.entity.OrderItem;
import com.example.project1.entity.Product;
import com.example.project1.entity.Review;
import com.example.project1.entity.User;
import com.example.project1.exception.ApiErrors;
import com.example.project1.repository.OrderItemRepository;
import com.example.project1.repository.ProductRepository;
import com.example.project1.repository.ReviewRepository;
import com.example.project1.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReviewServiceImpl implements ReviewService {

    private static final String VISIBLE = "VISIBLE";
    private static final String HIDDEN = "HIDDEN";
    private static final Set<String> STATUSES = Set.of(VISIBLE, HIDDEN);

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderItemRepository orderItemRepository;

    public ReviewServiceImpl(ReviewRepository reviewRepository,
                             ProductRepository productRepository,
                             UserRepository userRepository,
                             OrderItemRepository orderItemRepository) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.orderItemRepository = orderItemRepository;
    }

    // ------------------------------------------------------------------ public

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getVisibleReviews(Long productId) {
        return reviewRepository.findByProduct_ProductIdAndStatusOrderByCreatedAtDesc(productId, VISIBLE).stream()
                .map(r -> toResponse(r, false))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewSummaryResponse> getSummaries() {
        Map<Long, List<Review>> byProduct = reviewRepository.findByStatus(VISIBLE).stream()
                .collect(Collectors.groupingBy(r -> r.getProduct().getProductId(), LinkedHashMap::new,
                        Collectors.toList()));
        return byProduct.entrySet().stream()
                .map(e -> new ReviewSummaryResponse(e.getKey(),
                        Math.round(e.getValue().stream().mapToInt(Review::getRating).average().orElse(0) * 10) / 10.0,
                        e.getValue().size()))
                .collect(Collectors.toList());
    }

    // ------------------------------------------------------------------ customer

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getMyReviews(String email) {
        User user = currentUser(email);
        return reviewRepository.findByUser_IdOrderByCreatedAtDesc(user.getId()).stream()
                .map(r -> toResponse(r, false))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewableProductResponse> getReviewableProducts(String email) {
        User user = currentUser(email);
        Map<Long, Review> mine = reviewRepository.findByUser_Id(user.getId()).stream()
                .collect(Collectors.toMap(r -> r.getProduct().getProductId(), r -> r, (a, b) -> a));
        Map<Long, Product> bought = new LinkedHashMap<>();
        orderItemRepository.findByOrder_User_Email(user.getEmail()).stream()
                .filter(ReviewServiceImpl::isPaidPurchase)
                .sorted(Comparator.comparing((OrderItem i) -> i.getOrder().getOrderDate(),
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .forEach(i -> bought.putIfAbsent(i.getProduct().getProductId(), i.getProduct()));
        return bought.values().stream()
                .map(p -> {
                    Review r = mine.get(p.getProductId());
                    return new ReviewableProductResponse(p.getProductId(), p.getName(), p.getImageUrl(),
                            r == null ? null : r.getReviewId(), r == null ? null : r.getRating());
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ReviewResponse createReview(String email, ReviewRequest request) {
        User user = currentUser(email);
        validate(request);
        Product product = productRepository.findById(ApiErrors.required(request.getProductId(), "Please choose a product."))
                .orElseThrow(() -> ApiErrors.notFound("Product not found."));
        if (!hasBought(user, product.getProductId())) {
            throw ApiErrors.forbidden("You can review a product after you have bought and paid for it.");
        }
        if (reviewRepository.findByProduct_ProductIdAndUser_Id(product.getProductId(), user.getId()).isPresent()) {
            throw ApiErrors.conflict("You have already reviewed this product. You can edit your review instead.");
        }
        Review review = new Review();
        review.setProduct(product);
        review.setUser(user);
        review.setStatus(VISIBLE);
        apply(review, request);
        return toResponse(reviewRepository.save(review), false);
    }

    @Override
    @Transactional
    public ReviewResponse updateOwnReview(String email, Long reviewId, ReviewRequest request) {
        User user = currentUser(email);
        Review review = find(reviewId);
        if (!review.getUser().getId().equals(user.getId())) {
            throw ApiErrors.forbidden("You can only edit your own reviews.");
        }
        validate(request);
        apply(review, request);
        return toResponse(reviewRepository.save(review), false);
    }

    @Override
    @Transactional
    public String deleteReview(String email, Long reviewId, boolean staff) {
        Review review = find(reviewId);
        if (!staff) {
            User user = currentUser(email);
            if (!review.getUser().getId().equals(user.getId())) {
                throw ApiErrors.forbidden("You can only delete your own reviews.");
            }
        }
        reviewRepository.delete(review);
        return "Review deleted successfully";
    }

    // ------------------------------------------------------------------ staff

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getAllReviews() {
        return reviewRepository.findAll().stream()
                .sorted(Comparator.comparing(Review::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(r -> toResponse(r, true))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ReviewResponse updateStatus(Long reviewId, String status) {
        Review review = find(reviewId);
        String clean = status == null ? "" : status.trim().toUpperCase(Locale.ROOT);
        if (!STATUSES.contains(clean)) {
            throw ApiErrors.badRequest("Status must be VISIBLE or HIDDEN.");
        }
        review.setStatus(clean);
        return toResponse(reviewRepository.save(review), true);
    }

    // ------------------------------------------------------------------ helpers

    private Review find(Long id) {
        return reviewRepository.findById(ApiErrors.required(id, "Review id is required."))
                .orElseThrow(() -> ApiErrors.notFound("Review not found."));
    }

    private User currentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> ApiErrors.notFound("User not found."));
    }

    private boolean hasBought(User user, Long productId) {
        return orderItemRepository.findByOrder_User_Email(user.getEmail()).stream()
                .anyMatch(i -> i.getProduct().getProductId().equals(productId) && isPaidPurchase(i));
    }

    private static boolean isPaidPurchase(OrderItem item) {
        return item.getOrder() != null
                && "PAID".equalsIgnoreCase(item.getOrder().getPaymentStatus())
                && !"CANCELLED".equalsIgnoreCase(item.getOrder().getOrderStatus());
    }

    private static void validate(ReviewRequest request) {
        if (request == null) {
            throw ApiErrors.badRequest("Review details are required.");
        }
        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            throw ApiErrors.badRequest("Please choose a rating from 1 to 5 stars.");
        }
        String comment = ApiErrors.requiredText(request.getComment(), "Your review", 1000);
        if (comment.length() < 10) {
            throw ApiErrors.badRequest("Your review must be at least 10 characters.");
        }
        ApiErrors.optionalText(request.getTitle(), "Review title", 120);
    }

    private static void apply(Review review, ReviewRequest request) {
        review.setRating(request.getRating());
        review.setTitle(ApiErrors.optionalText(request.getTitle(), "Review title", 120));
        review.setComment(request.getComment().trim());
    }

    /** "Kasun Perera" -> "Kasun P." so the public never sees full names or e-mails. */
    static String displayName(String name) {
        if (name == null || name.isBlank()) {
            return "MelodyHub customer";
        }
        String[] parts = name.trim().split("\\s+");
        return parts.length == 1 ? parts[0] : parts[0] + " " + parts[parts.length - 1].charAt(0) + ".";
    }

    private ReviewResponse toResponse(Review review, boolean forStaff) {
        ReviewResponse response = new ReviewResponse();
        response.setReviewId(review.getReviewId());
        response.setProductId(review.getProduct().getProductId());
        response.setProductName(review.getProduct().getName());
        response.setProductImageUrl(review.getProduct().getImageUrl());
        response.setReviewerName(displayName(review.getUser().getName()));
        if (forStaff) {
            response.setUserEmail(review.getUser().getEmail());
        }
        response.setRating(review.getRating());
        response.setTitle(review.getTitle());
        response.setComment(review.getComment());
        response.setStatus(review.getStatus());
        response.setVerifiedPurchase(true); // reviews can only be written after a paid purchase
        response.setCreatedAt(review.getCreatedAt());
        response.setUpdatedAt(review.getUpdatedAt());
        return response;
    }
}
