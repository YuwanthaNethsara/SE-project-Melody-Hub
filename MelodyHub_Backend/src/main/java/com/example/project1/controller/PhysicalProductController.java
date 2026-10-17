package com.example.project1.controller;

import com.example.project1.dto.PhysicalProductRequest;
import com.example.project1.dto.PhysicalProductResponse;
import com.example.project1.service.PhysicalProductService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/physical-products")
public class PhysicalProductController {

    private final PhysicalProductService physicalProductService;

    public PhysicalProductController(
            PhysicalProductService physicalProductService) {

        this.physicalProductService = physicalProductService;
    }

    @PostMapping
    public ResponseEntity<PhysicalProductResponse>
    createPhysicalProduct(
            @RequestBody PhysicalProductRequest request) {

        return ResponseEntity.ok(
                physicalProductService
                        .createPhysicalProduct(request));
    }

    @GetMapping
    public ResponseEntity<List<PhysicalProductResponse>>
    getAllPhysicalProducts() {

        return ResponseEntity.ok(
                physicalProductService
                        .getAllPhysicalProducts());
    }

    @GetMapping("/{productId}")
    public ResponseEntity<PhysicalProductResponse>
    getPhysicalProductById(
            @PathVariable Long productId) {

        return ResponseEntity.ok(
                physicalProductService
                        .getPhysicalProductById(productId));
    }

    @PutMapping("/{productId}")
    public ResponseEntity<PhysicalProductResponse>
    updatePhysicalProduct(
            @PathVariable Long productId,
            @RequestBody PhysicalProductRequest request) {

        return ResponseEntity.ok(
                physicalProductService
                        .updatePhysicalProduct(
                                productId, request));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<String> deletePhysicalProduct(
            @PathVariable Long productId) {

        return ResponseEntity.ok(
                physicalProductService
                        .deletePhysicalProduct(productId));
    }
}
