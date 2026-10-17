package com.example.project1.controller;

import com.example.project1.dto.SupplierDeliveryRequest;
import com.example.project1.dto.SupplierDeliveryResponse;
import com.example.project1.service.SupplierDeliveryService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** ADMIN only (see SecurityConfig). */
@RestController
@RequestMapping("/api/supplier-deliveries")
public class SupplierDeliveryController {

    private final SupplierDeliveryService deliveryService;

    public SupplierDeliveryController(SupplierDeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @PostMapping
    public ResponseEntity<SupplierDeliveryResponse> createDelivery(
            @RequestBody SupplierDeliveryRequest request) {
        return new ResponseEntity<>(deliveryService.createDelivery(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<SupplierDeliveryResponse>> getAllDeliveries() {
        return ResponseEntity.ok(deliveryService.getAllDeliveries());
    }

    @GetMapping("/purchase-order/{purchaseOrderId}")
    public ResponseEntity<List<SupplierDeliveryResponse>> getByPurchaseOrder(
            @PathVariable Long purchaseOrderId) {
        return ResponseEntity.ok(deliveryService.getDeliveriesByPurchaseOrder(purchaseOrderId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SupplierDeliveryResponse> getDeliveryById(@PathVariable Long id) {
        return ResponseEntity.ok(deliveryService.getDeliveryById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SupplierDeliveryResponse> updateDelivery(
            @PathVariable Long id, @RequestBody SupplierDeliveryRequest request) {
        return ResponseEntity.ok(deliveryService.updateDelivery(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteDelivery(@PathVariable Long id) {
        return ResponseEntity.ok(deliveryService.deleteDelivery(id));
    }
}
