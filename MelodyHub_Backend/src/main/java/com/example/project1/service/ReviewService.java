package com.example.project1.service;

import com.example.project1.dto.ReviewRequest;
import com.example.project1.dto.ReviewResponse;
import com.example.project1.dto.ReviewSummaryResponse;
import com.example.project1.dto.ReviewableProductResponse;

import java.util.List;

/** Customer feedback: product ratings and comments. */
public interface ReviewService {

    /** Visible reviews of one product (public, newest first). */
    List<ReviewResponse> getVisibleReviews(Long productId);

    /** Average rating + count per product (public). */
    List<ReviewSummaryResponse> getSummaries();

    List<ReviewResponse> getMyReviews(String email);

    /** Products the customer has bought and paid for, with their review if any. */
    List<ReviewableProductResponse> getReviewableProducts(String email);

    ReviewResponse createReview(String email, ReviewRequest request);

    ReviewResponse updateOwnReview(String email, Long reviewId, ReviewRequest request);

    /** Customers delete their own review; staff may delete any. */
    String deleteReview(String email, Long reviewId, boolean staff);

    /** Staff: every review including hidden ones, with the reviewer's e-mail. */
    List<ReviewResponse> getAllReviews();

    /** Staff moderation: VISIBLE or HIDDEN. */
    ReviewResponse updateStatus(Long reviewId, String status);
}
