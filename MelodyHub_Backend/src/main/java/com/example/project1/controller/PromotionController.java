package com.example.project1.controller;

import com.example.project1.dto.PromotionRequest;
import com.example.project1.dto.PromotionResponse;
import com.example.project1.service.PromotionService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/promotions")
public class PromotionController {

    private final PromotionService promotionService;

    public PromotionController(
            PromotionService promotionService
    ) {
        this.promotionService = promotionService;
    }

    // ==========================================
    // CREATE
    // ==========================================

    @PostMapping
    public ResponseEntity<PromotionResponse>
    createPromotion(
            @RequestBody PromotionRequest request
    ) {

        return new ResponseEntity<>(
                promotionService.createPromotion(request),
                HttpStatus.CREATED
        );
    }

    // ==========================================
    // GET ALL
    // ==========================================

    @GetMapping
    public ResponseEntity<List<PromotionResponse>>
    getAllPromotions() {

        return ResponseEntity.ok(
                promotionService.getAllPromotions()
        );
    }

    // ==========================================
    // GET ACTIVE
    // ==========================================

    @GetMapping("/active")
    public ResponseEntity<List<PromotionResponse>>
    getActivePromotions() {

        return ResponseEntity.ok(
                promotionService.getActivePromotions()
        );
    }

    // ==========================================
    // GET BY PRODUCT
    // ==========================================

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<PromotionResponse>>
    getPromotionsByProduct(
            @PathVariable Long productId
    ) {

        return ResponseEntity.ok(
                promotionService
                        .getPromotionsByProduct(productId)
        );
    }

    // ==========================================
    // SEARCH
    // ==========================================

    @GetMapping("/search")
    public ResponseEntity<List<PromotionResponse>>
    searchPromotions(
            @RequestParam String name
    ) {

        return ResponseEntity.ok(
                promotionService
                        .searchPromotions(name)
        );
    }

    // ==========================================
    // GET BY ID
    // ==========================================

    @GetMapping("/{id}")
    public ResponseEntity<PromotionResponse>
    getPromotionById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                promotionService
                        .getPromotionById(id)
        );
    }

    // ==========================================
    // UPDATE
    // ==========================================

    @PutMapping("/{id}")
    public ResponseEntity<PromotionResponse>
    updatePromotion(
            @PathVariable Long id,
            @RequestBody PromotionRequest request
    ) {

        return ResponseEntity.ok(
                promotionService.updatePromotion(
                        id,
                        request
                )
        );
    }

    // ==========================================
    // DELETE
    // ==========================================

    @DeleteMapping("/{id}")
    public ResponseEntity<String>
    deletePromotion(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                promotionService.deletePromotion(id)
        );
    }
}
