package com.example.project1.service;

import com.example.project1.dto.InventoryRequest;
import com.example.project1.dto.InventoryResponse;

import java.util.List;

public interface InventoryService {

    InventoryResponse createInventory(InventoryRequest request);

    List<InventoryResponse> getAllInventory();

    InventoryResponse getInventoryById(Long id);

    InventoryResponse updateInventory(Long id, InventoryRequest request);

    String deleteInventory(Long id);

    InventoryResponse getInventoryByProductId(Long productId);
}
