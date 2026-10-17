package com.example.project1.service;

import com.example.project1.dto.PromotionRequest;
import com.example.project1.dto.PromotionResponse;

import java.util.List;

public interface PromotionService {

    PromotionResponse createPromotion(
            PromotionRequest request
    );

    List<PromotionResponse> getAllPromotions();

    List<PromotionResponse> getActivePromotions();

    PromotionResponse getPromotionById(Long id);

    List<PromotionResponse> getPromotionsByProduct(
            Long productId
    );

    List<PromotionResponse> searchPromotions(
            String name
    );

    PromotionResponse updatePromotion(
            Long id,
            PromotionRequest request
    );

    String deletePromotion(Long id);
}
