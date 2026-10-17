package com.example.project1.controller;

import com.example.project1.dto.FeaturedProductRequest;
import com.example.project1.dto.FeaturedProductResponse;
import com.example.project1.service.FeaturedProductService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/featured-products")
public class FeaturedProductController {

    private final FeaturedProductService featuredProductService;

    public FeaturedProductController(
            FeaturedProductService featuredProductService
    ) {
        this.featuredProductService = featuredProductService;
    }

    // ADD
    @PostMapping
    public ResponseEntity<FeaturedProductResponse>
    addFeaturedProduct(
            @RequestBody FeaturedProductRequest request
    ) {

        return new ResponseEntity<>(
                featuredProductService
                        .addFeaturedProduct(request),
                HttpStatus.CREATED
        );
    }

    // GET ALL
    @GetMapping
    public ResponseEntity<List<FeaturedProductResponse>>
    getAllFeaturedProducts() {

        return ResponseEntity.ok(
                featuredProductService
                        .getAllFeaturedProducts()
        );
    }

    // GET ACTIVE
    @GetMapping("/active")
    public ResponseEntity<List<FeaturedProductResponse>>
    getActiveFeaturedProducts() {

        return ResponseEntity.ok(
                featuredProductService
                        .getActiveFeaturedProducts()
        );
    }

    // GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<FeaturedProductResponse>
    getFeaturedProductById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                featuredProductService
                        .getFeaturedProductById(id)
        );
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<FeaturedProductResponse>
    updateFeaturedProduct(
            @PathVariable Long id,
            @RequestBody FeaturedProductRequest request
    ) {

        return ResponseEntity.ok(
                featuredProductService
                        .updateFeaturedProduct(
                                id,
                                request
                        )
        );
    }

    // REMOVE
    @DeleteMapping("/{id}")
    public ResponseEntity<String>
    removeFeaturedProduct(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                featuredProductService
                        .removeFeaturedProduct(id)
        );
    }
}
