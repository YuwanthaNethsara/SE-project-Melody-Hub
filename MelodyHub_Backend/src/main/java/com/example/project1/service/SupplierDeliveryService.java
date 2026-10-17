package com.example.project1.service;

import com.example.project1.dto.SupplierDeliveryRequest;
import com.example.project1.dto.SupplierDeliveryResponse;

import java.util.List;

public interface SupplierDeliveryService {

    SupplierDeliveryResponse createDelivery(SupplierDeliveryRequest request);

    List<SupplierDeliveryResponse> getAllDeliveries();

    SupplierDeliveryResponse getDeliveryById(Long id);

    List<SupplierDeliveryResponse> getDeliveriesByPurchaseOrder(Long purchaseOrderId);

    SupplierDeliveryResponse updateDelivery(Long id, SupplierDeliveryRequest request);

    String deleteDelivery(Long id);
}
