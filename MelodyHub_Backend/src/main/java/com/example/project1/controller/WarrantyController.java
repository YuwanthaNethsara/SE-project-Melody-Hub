package com.example.project1.controller;

import com.example.project1.dto.WarrantyRequest;
import com.example.project1.dto.WarrantyResponse;
import com.example.project1.service.WarrantyService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/warranties")
public class WarrantyController {

    private final WarrantyService warrantyService;

    public WarrantyController(
            WarrantyService warrantyService
    ) {
        this.warrantyService = warrantyService;
    }


    // =========================
    // CREATE WARRANTY
    // ADMIN ONLY
    // =========================

    @PostMapping
    public ResponseEntity<WarrantyResponse> createWarranty(
            @RequestBody WarrantyRequest request
    ) {

        WarrantyResponse response =
                warrantyService.createWarranty(request);

        return new ResponseEntity<>(
                response,
                HttpStatus.CREATED
        );
    }


    // =========================
    // GET ALL WARRANTIES
    // =========================

    @GetMapping
    public ResponseEntity<List<WarrantyResponse>>
    getAllWarranties() {

        return ResponseEntity.ok(
                warrantyService.getAllWarranties()
        );
    }


    // =========================
    // GET WARRANTY BY ID
    // =========================

    @GetMapping("/{id}")
    public ResponseEntity<WarrantyResponse>
    getWarrantyById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                warrantyService.getWarrantyById(id)
        );
    }


    // =========================
    // GET WARRANTY BY PRODUCT
    // =========================

    // 200 with the warranty, or 204 No Content when the product has none
    // (not an error: most products simply have no warranty)
    @GetMapping("/product/{productId}")
    public ResponseEntity<WarrantyResponse>
    getWarrantyByProduct(
            @PathVariable Long productId
    ) {

        WarrantyResponse warranty = warrantyService.findWarrantyByProduct(productId);
        return warranty == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(warranty);
    }


    // =========================
    // UPDATE WARRANTY
    // ADMIN ONLY
    // =========================

    @PutMapping("/{id}")
    public ResponseEntity<WarrantyResponse>
    updateWarranty(
            @PathVariable Long id,
            @RequestBody WarrantyRequest request
    ) {

        return ResponseEntity.ok(
                warrantyService.updateWarranty(
                        id,
                        request
                )
        );
    }


    // =========================
    // DELETE WARRANTY
    // ADMIN ONLY
    // =========================

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteWarranty(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                warrantyService.deleteWarranty(id)
        );
    }
}
