package com.example.project1.service;

import com.example.project1.exception.ApiErrors;

import com.example.project1.dto.InventoryRequest;
import com.example.project1.dto.InventoryResponse;
import com.example.project1.entity.Inventory;
import com.example.project1.entity.Product;
import com.example.project1.enums.StockStatus;
import com.example.project1.repository.InventoryRepository;
import com.example.project1.repository.ProductRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;

    public InventoryServiceImpl(
            InventoryRepository inventoryRepository,
            ProductRepository productRepository) {

        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
    }

    @Override
    public InventoryResponse createInventory(
            InventoryRequest request) {

        Product product = productRepository.findById(
                ApiErrors.required(request.getProductId(), "Please choose a product.")
        ).orElseThrow(() -> ApiErrors.notFound("The selected product was not found."));

        if (inventoryRepository.existsByProduct_ProductId(
                request.getProductId())) {

            throw ApiErrors.conflict(
                    "This product already has a stock record. Edit the existing record instead.");
        }

        validateLocation(request.getLocationCode());

        Inventory inventory = new Inventory();

        inventory.setProduct(product);
        inventory.setLocationCode(cleanLocation(request.getLocationCode()));
        inventory.setQuantityAvailable(
                request.getQuantityAvailable());
        inventory.setReorderLevel(
                request.getReorderLevel());

        inventory.setStockStatus(
                calculateStockStatus(
                        request.getQuantityAvailable(),
                        request.getReorderLevel()
                )
        );

        Inventory saved = inventoryRepository.save(inventory);

        return convertToResponse(saved);
    }

    @Override
    public List<InventoryResponse> getAllInventory() {

        return inventoryRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public InventoryResponse getInventoryById(Long id) {

        Inventory inventory = inventoryRepository.findById(
                        ApiErrors.required(id, "Stock record id is required."))
                .orElseThrow(() -> ApiErrors.notFound("Stock record not found."));

        return convertToResponse(inventory);
    }

    @Override
    public InventoryResponse updateInventory(
            Long id,
            InventoryRequest request) {

        Inventory inventory = inventoryRepository.findById(
                        ApiErrors.required(id, "Stock record id is required."))
                .orElseThrow(() -> ApiErrors.notFound("Stock record not found."));

        validateLocation(request.getLocationCode());
        inventory.setLocationCode(
                cleanLocation(request.getLocationCode()));

        inventory.setQuantityAvailable(
                request.getQuantityAvailable());

        inventory.setReorderLevel(
                request.getReorderLevel());

        inventory.setStockStatus(
                calculateStockStatus(
                        request.getQuantityAvailable(),
                        request.getReorderLevel()
                )
        );

        Inventory updated = inventoryRepository.save(inventory);

        return convertToResponse(updated);
    }

    @Override
    public String deleteInventory(Long id) {

        if (!inventoryRepository.existsById(ApiErrors.required(id, "Stock record id is required."))) {
            throw ApiErrors.notFound("Stock record not found.");
        }

        inventoryRepository.deleteById(id);

        return "Inventory deleted successfully";
    }

    @Override
    public InventoryResponse getInventoryByProductId(
            Long productId) {

        Inventory inventory =
                inventoryRepository
                        .findByProduct_ProductId(productId)
                        .orElseThrow(() -> ApiErrors.notFound(
                                "This product has no stock record yet."));

        return convertToResponse(inventory);
    }

    private StockStatus calculateStockStatus(
            Integer quantity,
            Integer reorderLevel) {
        return StockRules.statusFor(quantity, reorderLevel);
    }

    private static String cleanLocation(String location) {
        return location == null || location.isBlank() ? null : location.trim();
    }

    private static void validateLocation(String location) {
        if (location != null && !location.isBlank()
                && !location.trim().matches("^[A-Za-z0-9][A-Za-z0-9 \\-_/.]{0,49}$")) {
            throw ApiErrors.badRequest("Location code may contain letters, numbers, spaces, - _ / . (max 50).");
        }
    }

    private InventoryResponse convertToResponse(
            Inventory inventory) {

        return new InventoryResponse(
                inventory.getInventoryId(),
                inventory.getProduct().getProductId(),
                inventory.getProduct().getName(),
                inventory.getLocationCode(),
                inventory.getQuantityAvailable(),
                inventory.getReorderLevel(),
                inventory.getStockStatus(),
                inventory.getLastUpdated()
        );
    }
}
