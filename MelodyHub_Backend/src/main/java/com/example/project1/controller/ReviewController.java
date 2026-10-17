package com.example.project1.controller;

import com.example.project1.dto.ReviewRequest;
import com.example.project1.dto.ReviewResponse;
import com.example.project1.dto.ReviewSummaryResponse;
import com.example.project1.dto.ReviewableProductResponse;
import com.example.project1.service.ReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Customer feedback / product reviews.
 *  public          GET /product/{productId}, GET /summary
 *  CUSTOMER        GET /my, GET /eligible, POST, PUT /{id} (own), DELETE /{id} (own)
 *  ADMIN, MARKETING_MANAGER  GET (all), PUT /{id}/status, DELETE /{id}
 * (role rules in SecurityConfig, ownership checks in ReviewServiceImpl)
 */
@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ReviewResponse>> productReviews(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.getVisibleReviews(productId));
    }

    @GetMapping("/summary")
    public ResponseEntity<List<ReviewSummaryResponse>> summaries() {
        return ResponseEntity.ok(reviewService.getSummaries());
    }

    @GetMapping("/my")
    public ResponseEntity<List<ReviewResponse>> myReviews(Authentication authentication) {
        return ResponseEntity.ok(reviewService.getMyReviews(authentication.getName()));
    }

    @GetMapping("/eligible")
    public ResponseEntity<List<ReviewableProductResponse>> eligible(Authentication authentication) {
        return ResponseEntity.ok(reviewService.getReviewableProducts(authentication.getName()));
    }

    @PostMapping
    public ResponseEntity<ReviewResponse> create(@RequestBody ReviewRequest request,
                                                 Authentication authentication) {
        return new ResponseEntity<>(reviewService.createReview(authentication.getName(), request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReviewResponse> update(@PathVariable Long id,
                                                 @RequestBody ReviewRequest request,
                                                 Authentication authentication) {
        return ResponseEntity.ok(reviewService.updateOwnReview(authentication.getName(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id, Authentication authentication) {
        boolean staff = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()) || "ROLE_MARKETING_MANAGER".equals(a.getAuthority()));
        return ResponseEntity.ok(Map.of("message", reviewService.deleteReview(authentication.getName(), id, staff)));
    }

    @GetMapping
    public ResponseEntity<List<ReviewResponse>> all() {
        return ResponseEntity.ok(reviewService.getAllReviews());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ReviewResponse> updateStatus(@PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(reviewService.updateStatus(id, status));
    }
}
