package com.example.project1.service;

import com.example.project1.dto.PurchaseOrderRequest;
import com.example.project1.dto.PurchaseOrderResponse;

import java.util.List;

public interface PurchaseOrderService {

    PurchaseOrderResponse createPurchaseOrder(
            PurchaseOrderRequest request
    );

    List<PurchaseOrderResponse> getAllPurchaseOrders();

    PurchaseOrderResponse getPurchaseOrderById(
            Long id
    );

    List<PurchaseOrderResponse> getOrdersBySupplier(
            Long supplierId
    );

    List<PurchaseOrderResponse> getOrdersByProduct(
            Long productId
    );

    List<PurchaseOrderResponse> getOrdersByStatus(
            String status
    );

    PurchaseOrderResponse updatePurchaseOrder(
            Long id,
            PurchaseOrderRequest request
    );

    String deletePurchaseOrder(Long id);
}
