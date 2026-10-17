package com.example.project1.dto;

/** A product the logged-in customer bought (and paid for), with their review if they wrote one. */
public class ReviewableProductResponse {

    private Long productId;
    private String productName;
    private String imageUrl;
    private Long reviewId;
    private Integer rating;

    public ReviewableProductResponse() {
    }

    public ReviewableProductResponse(Long productId, String productName, String imageUrl,
                                     Long reviewId, Integer rating) {
        this.productId = productId;
        this.productName = productName;
        this.imageUrl = imageUrl;
        this.reviewId = reviewId;
        this.rating = rating;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Long getReviewId() {
        return reviewId;
    }

    public Integer getRating() {
        return rating;
    }
}
