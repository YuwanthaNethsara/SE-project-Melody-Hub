package com.example.project1.dto;

/** Average rating and number of visible reviews of one product. */
public class ReviewSummaryResponse {

    private Long productId;
    private double averageRating;
    private long reviewCount;

    public ReviewSummaryResponse() {
    }

    public ReviewSummaryResponse(Long productId, double averageRating, long reviewCount) {
        this.productId = productId;
        this.averageRating = averageRating;
        this.reviewCount = reviewCount;
    }

    public Long getProductId() {
        return productId;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public long getReviewCount() {
        return reviewCount;
    }
}
