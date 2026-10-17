package com.example.project1.controller;

import com.example.project1.dto.PurchaseOrderRequest;
import com.example.project1.dto.PurchaseOrderResponse;
import com.example.project1.service.PurchaseOrderService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchase-orders")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    public PurchaseOrderController(
            PurchaseOrderService purchaseOrderService
    ) {
        this.purchaseOrderService = purchaseOrderService;
    }

    // CREATE
    @PostMapping
    public ResponseEntity<PurchaseOrderResponse>
    createPurchaseOrder(
            @RequestBody PurchaseOrderRequest request
    ) {

        return new ResponseEntity<>(
                purchaseOrderService
                        .createPurchaseOrder(request),
                HttpStatus.CREATED
        );
    }

    // GET ALL
    @GetMapping
    public ResponseEntity<List<PurchaseOrderResponse>>
    getAllPurchaseOrders() {

        return ResponseEntity.ok(
                purchaseOrderService
                        .getAllPurchaseOrders()
        );
    }

    // GET BY SUPPLIER
    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<List<PurchaseOrderResponse>>
    getOrdersBySupplier(
            @PathVariable Long supplierId
    ) {

        return ResponseEntity.ok(
                purchaseOrderService
                        .getOrdersBySupplier(supplierId)
        );
    }

    // GET BY PRODUCT
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<PurchaseOrderResponse>>
    getOrdersByProduct(
            @PathVariable Long productId
    ) {

        return ResponseEntity.ok(
                purchaseOrderService
                        .getOrdersByProduct(productId)
        );
    }

    // GET BY STATUS
    @GetMapping("/status/{status}")
    public ResponseEntity<List<PurchaseOrderResponse>>
    getOrdersByStatus(
            @PathVariable String status
    ) {

        return ResponseEntity.ok(
                purchaseOrderService
                        .getOrdersByStatus(status)
        );
    }

    // GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<PurchaseOrderResponse>
    getPurchaseOrderById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                purchaseOrderService
                        .getPurchaseOrderById(id)
        );
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<PurchaseOrderResponse>
    updatePurchaseOrder(
            @PathVariable Long id,
            @RequestBody PurchaseOrderRequest request
    ) {

        return ResponseEntity.ok(
                purchaseOrderService
                        .updatePurchaseOrder(
                                id,
                                request
                        )
        );
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<String>
    deletePurchaseOrder(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                purchaseOrderService
                        .deletePurchaseOrder(id)
        );
    }
}
