package com.example.project1.controller;

import com.example.project1.dto.AdvertisementRequest;
import com.example.project1.dto.AdvertisementResponse;
import com.example.project1.service.AdvertisementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Management: ADMIN and MARKETING_MANAGER (see SecurityConfig).
 * GET /api/advertisements/active is public (store front banners).
 *
 * Create / update accept either
 *  - multipart/form-data: part "data" (JSON AdvertisementRequest) + optional file part "image"
 *  - application/json:    AdvertisementRequest (imageUrl kept for API compatibility)
 */
@RestController
@RequestMapping("/api/advertisements")
public class AdvertisementController {

    private final AdvertisementService advertisementService;

    public AdvertisementController(AdvertisementService advertisementService) {
        this.advertisementService = advertisementService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AdvertisementResponse> createWithImage(
            @RequestPart("data") AdvertisementRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        return new ResponseEntity<>(
                advertisementService.createAdvertisement(request, image), HttpStatus.CREATED);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AdvertisementResponse> createAdvertisement(
            @RequestBody AdvertisementRequest request) {
        return new ResponseEntity<>(
                advertisementService.createAdvertisement(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<AdvertisementResponse>> getAllAdvertisements() {
        return ResponseEntity.ok(advertisementService.getAllAdvertisements());
    }

    @GetMapping("/active")
    public ResponseEntity<List<AdvertisementResponse>> getActiveAdvertisements() {
        return ResponseEntity.ok(advertisementService.getActiveAdvertisements());
    }

    @GetMapping("/search")
    public ResponseEntity<List<AdvertisementResponse>> searchAdvertisements(
            @RequestParam String title) {
        return ResponseEntity.ok(advertisementService.searchAdvertisements(title));
    }

    @GetMapping("/campaign/{campaignId}")
    public ResponseEntity<List<AdvertisementResponse>> getByCampaign(
            @PathVariable Long campaignId) {
        return ResponseEntity.ok(advertisementService.getAdvertisementsByCampaign(campaignId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdvertisementResponse> getAdvertisementById(@PathVariable Long id) {
        return ResponseEntity.ok(advertisementService.getAdvertisementById(id));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AdvertisementResponse> updateWithImage(
            @PathVariable Long id,
            @RequestPart("data") AdvertisementRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        return ResponseEntity.ok(advertisementService.updateAdvertisement(id, request, image));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AdvertisementResponse> updateAdvertisement(
            @PathVariable Long id, @RequestBody AdvertisementRequest request) {
        return ResponseEntity.ok(advertisementService.updateAdvertisement(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAdvertisement(@PathVariable Long id) {
        return ResponseEntity.ok(advertisementService.deleteAdvertisement(id));
    }
}
