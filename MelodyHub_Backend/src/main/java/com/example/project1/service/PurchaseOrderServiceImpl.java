package com.example.project1.service;

import com.example.project1.dto.PurchaseOrderRequest;
import com.example.project1.dto.PurchaseOrderResponse;
import com.example.project1.entity.Product;
import com.example.project1.entity.PurchaseOrder;
import com.example.project1.entity.Supplier;
import com.example.project1.repository.ProductRepository;
import com.example.project1.repository.PurchaseOrderRepository;
import com.example.project1.repository.SupplierRepository;

import com.example.project1.repository.SupplierDeliveryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PurchaseOrderServiceImpl
        implements PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;

    private final SupplierRepository supplierRepository;

    private final ProductRepository productRepository;

    private final SupplierDeliveryRepository supplierDeliveryRepository;

    public PurchaseOrderServiceImpl(
            PurchaseOrderRepository purchaseOrderRepository,
            SupplierRepository supplierRepository,
            ProductRepository productRepository,
            SupplierDeliveryRepository supplierDeliveryRepository
    ) {
        this.supplierDeliveryRepository = supplierDeliveryRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
    }

    // ==========================================
    // CREATE
    // ==========================================

    @Override
    public PurchaseOrderResponse createPurchaseOrder(
            PurchaseOrderRequest request
    ) {

        validateRequest(request);

        Supplier supplier =
                supplierRepository
                        .findById(request.getSupplierId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Supplier not found"
                                )
                        );

        Product product =
                productRepository
                        .findById(request.getProductId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Product not found"
                                )
                        );

        PurchaseOrder purchaseOrder =
                new PurchaseOrder();

        purchaseOrder.setSupplier(supplier);

        purchaseOrder.setProduct(product);

        purchaseOrder.setQuantity(
                request.getQuantity()
        );

        purchaseOrder.setUnitPrice(
                request.getUnitPrice()
        );

        purchaseOrder.setTotalAmount(
                request.getQuantity()
                        * request.getUnitPrice()
        );

        purchaseOrder.setOrderDate(
                LocalDateTime.now()
        );

        if (request.getStatus() == null ||
                request.getStatus().trim().isEmpty()) {

            purchaseOrder.setStatus("PENDING");

        } else {

            purchaseOrder.setStatus(
                    request.getStatus().toUpperCase()
            );
        }

        PurchaseOrder saved =
                purchaseOrderRepository.save(
                        purchaseOrder
                );

        return convertToResponse(saved);
    }

    // ==========================================
    // GET ALL
    // ==========================================

    @Override
    public List<PurchaseOrderResponse>
    getAllPurchaseOrders() {

        return purchaseOrderRepository
                .findAll()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // ==========================================
    // GET BY ID
    // ==========================================

    @Override
    public PurchaseOrderResponse
    getPurchaseOrderById(Long id) {

        PurchaseOrder purchaseOrder =
                purchaseOrderRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Purchase order not found"
                                )
                        );

        return convertToResponse(purchaseOrder);
    }

    // ==========================================
    // GET BY SUPPLIER
    // ==========================================

    @Override
    public List<PurchaseOrderResponse>
    getOrdersBySupplier(Long supplierId) {

        return purchaseOrderRepository
                .findBySupplier_SupplierId(supplierId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // ==========================================
    // GET BY PRODUCT
    // ==========================================

    @Override
    public List<PurchaseOrderResponse>
    getOrdersByProduct(Long productId) {

        return purchaseOrderRepository
                .findByProduct_ProductId(productId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // ==========================================
    // GET BY STATUS
    // ==========================================

    @Override
    public List<PurchaseOrderResponse>
    getOrdersByStatus(String status) {

        return purchaseOrderRepository
                .findByStatus(status.toUpperCase())
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // ==========================================
    // UPDATE
    // ==========================================

    @Override
    public PurchaseOrderResponse updatePurchaseOrder(
            Long id,
            PurchaseOrderRequest request
    ) {

        validateRequest(request);

        PurchaseOrder purchaseOrder =
                purchaseOrderRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Purchase order not found"
                                )
                        );

        Supplier supplier =
                supplierRepository
                        .findById(request.getSupplierId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Supplier not found"
                                )
                        );

        Product product =
                productRepository
                        .findById(request.getProductId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Product not found"
                                )
                        );

        purchaseOrder.setSupplier(supplier);

        purchaseOrder.setProduct(product);

        purchaseOrder.setQuantity(
                request.getQuantity()
        );

        purchaseOrder.setUnitPrice(
                request.getUnitPrice()
        );

        purchaseOrder.setTotalAmount(
                request.getQuantity()
                        * request.getUnitPrice()
        );

        if (request.getStatus() != null &&
                !request.getStatus().trim().isEmpty()) {

            purchaseOrder.setStatus(
                    request.getStatus().toUpperCase()
            );
        }

        PurchaseOrder updated =
                purchaseOrderRepository.save(
                        purchaseOrder
                );

        return convertToResponse(updated);
    }

    // ==========================================
    // DELETE
    // ==========================================

    @Override
    public String deletePurchaseOrder(Long id) {

        if (!purchaseOrderRepository.existsById(id)) {

            throw new RuntimeException(
                    "Purchase order not found"
            );
        }

        if (supplierDeliveryRepository.existsByPurchaseOrder_PurchaseOrderId(id)) {

            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Deliveries have been recorded for this purchase order. Delete those deliveries first.");
        }

        purchaseOrderRepository.deleteById(id);

        return "Purchase order deleted successfully";
    }

    // ==========================================
    // VALIDATION
    // ==========================================

    private void validateRequest(
            PurchaseOrderRequest request
    ) {

        if (request.getSupplierId() == null) {

            throw new RuntimeException(
                    "Supplier ID is required"
            );
        }

        if (request.getProductId() == null) {

            throw new RuntimeException(
                    "Product ID is required"
            );
        }

        if (request.getQuantity() == null ||
                request.getQuantity() <= 0) {

            throw new RuntimeException(
                    "Quantity must be greater than 0"
            );
        }

        if (request.getUnitPrice() == null ||
                request.getUnitPrice() <= 0) {

            throw new RuntimeException(
                    "Unit price must be greater than 0"
            );
        }

        if (request.getStatus() != null &&
                !request.getStatus().trim().isEmpty()) {

            String status =
                    request.getStatus().toUpperCase();

            if (!status.equals("PENDING") &&
                    !status.equals("ORDERED") &&
                    !status.equals("RECEIVED") &&
                    !status.equals("CANCELLED")) {

                throw new RuntimeException(
                        "Invalid purchase order status"
                );
            }
        }
    }

    // ==========================================
    // CONVERT
    // ==========================================

    private PurchaseOrderResponse
    convertToResponse(
            PurchaseOrder purchaseOrder
    ) {

        Supplier supplier =
                purchaseOrder.getSupplier();

        Product product =
                purchaseOrder.getProduct();

        return new PurchaseOrderResponse(

                purchaseOrder.getPurchaseOrderId(),

                supplier.getSupplierId(),

                supplier.getSupplierName(),

                product.getProductId(),

                product.getName(),

                purchaseOrder.getQuantity(),

                purchaseOrder.getUnitPrice(),

                purchaseOrder.getTotalAmount(),

                purchaseOrder.getOrderDate(),

                purchaseOrder.getStatus()
        );
    }
}
