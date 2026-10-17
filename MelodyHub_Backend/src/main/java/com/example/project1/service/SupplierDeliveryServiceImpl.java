package com.example.project1.service;

import com.example.project1.dto.SupplierDeliveryRequest;
import com.example.project1.dto.SupplierDeliveryResponse;
import com.example.project1.entity.PurchaseOrder;
import com.example.project1.entity.SupplierDelivery;
import com.example.project1.repository.PurchaseOrderRepository;
import com.example.project1.repository.SupplierDeliveryRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Deliveries received against purchase orders.
 * Business rules: the purchase order must exist and not be cancelled, the delivery date cannot be
 * in the future or before the order date, and the accepted quantities of all deliveries together
 * can never exceed what was ordered. (Stock levels are still maintained on the Inventory page.)
 */
@Service
public class SupplierDeliveryServiceImpl implements SupplierDeliveryService {

    private static final Set<String> STATUSES = Set.of("RECEIVED", "PARTIAL", "REJECTED");

    private final SupplierDeliveryRepository deliveryRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;

    public SupplierDeliveryServiceImpl(SupplierDeliveryRepository deliveryRepository,
                                       PurchaseOrderRepository purchaseOrderRepository) {
        this.deliveryRepository = deliveryRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
    }

    @Override
    public SupplierDeliveryResponse createDelivery(SupplierDeliveryRequest request) {

        basicValidation(request);

        PurchaseOrder po = findPurchaseOrder(request.getPurchaseOrderId());

        checkRules(po, request, null);

        SupplierDelivery delivery = new SupplierDelivery();
        apply(delivery, po, request);
        delivery.setCreatedDate(LocalDateTime.now());

        return convertToResponse(deliveryRepository.save(delivery));
    }

    @Override
    public List<SupplierDeliveryResponse> getAllDeliveries() {
        return deliveryRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public SupplierDeliveryResponse getDeliveryById(Long id) {
        return convertToResponse(find(id));
    }

    @Override
    public List<SupplierDeliveryResponse> getDeliveriesByPurchaseOrder(Long purchaseOrderId) {
        return deliveryRepository.findByPurchaseOrder_PurchaseOrderId(purchaseOrderId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public SupplierDeliveryResponse updateDelivery(Long id, SupplierDeliveryRequest request) {

        SupplierDelivery delivery = find(id);

        basicValidation(request);

        PurchaseOrder po = findPurchaseOrder(request.getPurchaseOrderId());

        checkRules(po, request, id);

        apply(delivery, po, request);

        return convertToResponse(deliveryRepository.save(delivery));
    }

    @Override
    public String deleteDelivery(Long id) {

        SupplierDelivery delivery = find(id);

        deliveryRepository.delete(delivery);

        return "Delivery deleted successfully";
    }

    private SupplierDelivery find(Long id) {
        return deliveryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));
    }

    private PurchaseOrder findPurchaseOrder(Long id) {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Purchase order not found"));
    }

    private void basicValidation(SupplierDeliveryRequest request) {

        if (request.getPurchaseOrderId() == null) {
            throw new IllegalArgumentException("Purchase order is required");
        }
        if (request.getDeliveryDate() == null) {
            throw new IllegalArgumentException("Delivery date is required");
        }
        if (request.getQuantityReceived() == null || request.getQuantityReceived() <= 0) {
            throw new IllegalArgumentException("Quantity received must be greater than 0");
        }
        if (request.getStatus() == null || request.getStatus().isBlank()
                || !STATUSES.contains(request.getStatus().trim().toUpperCase())) {
            throw new IllegalArgumentException("Status must be RECEIVED, PARTIAL or REJECTED");
        }
        if (request.getNotes() != null && request.getNotes().trim().length() > 500) {
            throw new IllegalArgumentException("Notes must be at most 500 characters");
        }
    }

    private void checkRules(PurchaseOrder po, SupplierDeliveryRequest request, Long currentId) {

        if ("CANCELLED".equalsIgnoreCase(po.getStatus())) {
            throw new IllegalStateException("A delivery cannot be recorded for a cancelled purchase order");
        }

        LocalDate date = request.getDeliveryDate();

        if (date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Delivery date cannot be in the future");
        }
        if (po.getOrderDate() != null && date.isBefore(po.getOrderDate().toLocalDate())) {
            throw new IllegalArgumentException("Delivery date cannot be before the purchase order date");
        }

        // rejected goods were not accepted, so they do not count against the ordered quantity
        if (!"REJECTED".equalsIgnoreCase(request.getStatus().trim())) {

            int alreadyAccepted = deliveryRepository
                    .findByPurchaseOrder_PurchaseOrderId(po.getPurchaseOrderId()).stream()
                    .filter(d -> currentId == null || !d.getDeliveryId().equals(currentId))
                    .filter(d -> !"REJECTED".equalsIgnoreCase(d.getStatus()))
                    .mapToInt(SupplierDelivery::getQuantityReceived)
                    .sum();

            if (alreadyAccepted + request.getQuantityReceived() > po.getQuantity()) {
                throw new IllegalArgumentException(
                        "Quantity received exceeds the quantity ordered (" + po.getQuantity()
                                + " ordered, " + alreadyAccepted + " already received)");
            }
        }
    }

    private void apply(SupplierDelivery delivery, PurchaseOrder po, SupplierDeliveryRequest request) {
        delivery.setPurchaseOrder(po);
        delivery.setDeliveryDate(request.getDeliveryDate());
        delivery.setQuantityReceived(request.getQuantityReceived());
        delivery.setStatus(request.getStatus().trim().toUpperCase());
        delivery.setNotes(request.getNotes() == null || request.getNotes().isBlank()
                ? null : request.getNotes().trim());
    }

    private SupplierDeliveryResponse convertToResponse(SupplierDelivery d) {
        PurchaseOrder po = d.getPurchaseOrder();
        return new SupplierDeliveryResponse(
                d.getDeliveryId(),
                po.getPurchaseOrderId(),
                po.getSupplier().getSupplierName(),
                po.getProduct().getName(),
                po.getQuantity(),
                d.getDeliveryDate(),
                d.getQuantityReceived(),
                d.getStatus(),
                d.getNotes(),
                d.getCreatedDate());
    }
}
