package com.example.project1.service;

import com.example.project1.dto.OrderItemRequest;
import com.example.project1.dto.OrderItemResponse;
import com.example.project1.entity.Inventory;
import com.example.project1.entity.Order;
import com.example.project1.entity.OrderItem;
import com.example.project1.entity.Product;
import com.example.project1.exception.ApiErrors;
import com.example.project1.repository.InventoryRepository;
import com.example.project1.repository.OrderItemRepository;
import com.example.project1.repository.OrderRepository;
import com.example.project1.repository.ProductRepository;
import com.example.project1.repository.WarrantyClaimRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Order lines. Customers create lines only through checkout (OrderServiceImpl);
 * the endpoints that change lines directly are ADMIN only and keep stock and totals correct:
 * the order must still be PENDING or CONFIRMED and unpaid, prices come from the server and
 * reserved stock is adjusted.
 */
@Service
public class OrderItemServiceImpl implements OrderItemService {

    private static final int MAX_QUANTITY = 99;

    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final PricingService pricingService;
    private final WarrantyClaimRepository warrantyClaimRepository;

    public OrderItemServiceImpl(OrderItemRepository orderItemRepository,
                                OrderRepository orderRepository,
                                ProductRepository productRepository,
                                InventoryRepository inventoryRepository,
                                PricingService pricingService,
                                WarrantyClaimRepository warrantyClaimRepository) {
        this.orderItemRepository = orderItemRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.pricingService = pricingService;
        this.warrantyClaimRepository = warrantyClaimRepository;
    }

    @Override
    @Transactional
    public OrderItemResponse createOrderItem(OrderItemRequest request) {
        if (request == null) {
            throw ApiErrors.badRequest("Order item details are required.");
        }
        Order order = orderRepository.findById(ApiErrors.required(request.getOrderId(), "Please choose an order."))
                .orElseThrow(() -> ApiErrors.notFound("Order not found."));
        requireEditable(order);
        Product product = productRepository.findById(ApiErrors.required(request.getProductId(), "Please choose a product."))
                .orElseThrow(() -> ApiErrors.notFound("Product not found."));
        int quantity = validQuantity(request.getQuantity());
        StockRules.requireSellable(product);
        changeStock(product, -quantity);

        BigDecimal unitPrice = pricingService.unitPrice(product);
        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setQuantity(quantity);
        orderItem.setUnitPrice(unitPrice.doubleValue());
        orderItem.setSubtotal(pricingService.lineTotal(unitPrice, quantity));
        OrderItem saved = orderItemRepository.save(orderItem);
        updateOrderTotal(order);
        return convertToResponse(saved);
    }

    @Override
    public List<OrderItemResponse> getAllOrderItems() {
        return orderItemRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public OrderItemResponse getOrderItemById(Long id) {
        return orderItemRepository.findById(id).map(this::convertToResponse).orElse(null);
    }

    @Override
    public List<OrderItemResponse> getItemsByOrder(Long orderId) {
        return orderItemRepository
                .findByOrder_OrderId(orderId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderItemResponse> getItemsByUserEmail(String email) {
        return orderItemRepository
                .findByOrder_User_Email(email)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public String getOwnerEmail(Long orderItemId) {
        return orderItemRepository
                .findById(orderItemId)
                .map(i -> i.getOrder().getUser().getEmail())
                .orElse(null);
    }

    @Override
    public String getOrderOwnerEmail(Long orderId) {
        return orderRepository
                .findById(orderId)
                .map(o -> o.getUser().getEmail())
                .orElse(null);
    }

    @Override
    @Transactional
    public OrderItemResponse updateQuantity(Long id, Integer quantity) {
        int newQuantity = validQuantity(quantity);
        OrderItem item = orderItemRepository.findById(ApiErrors.required(id, "Order item id is required."))
                .orElseThrow(() -> ApiErrors.notFound("Order item not found."));
        requireEditable(item.getOrder());
        int difference = newQuantity - item.getQuantity();
        if (difference != 0) {
            changeStock(item.getProduct(), -difference);
        }
        item.setQuantity(newQuantity);
        item.setSubtotal(pricingService.lineTotal(BigDecimal.valueOf(item.getUnitPrice()), newQuantity));
        OrderItem updated = orderItemRepository.save(item);
        updateOrderTotal(item.getOrder());
        return convertToResponse(updated);
    }

    @Override
    @Transactional
    public String deleteOrderItem(Long id) {
        OrderItem item = orderItemRepository.findById(ApiErrors.required(id, "Order item id is required."))
                .orElseThrow(() -> ApiErrors.notFound("Order item not found."));
        Order order = item.getOrder();
        requireEditable(order);
        if (warrantyClaimRepository.existsByOrderItem_OrderItemId(id)) {
            throw ApiErrors.conflict("A warranty claim exists for this item, so it cannot be removed.");
        }
        if (orderItemRepository.findByOrder_OrderId(order.getOrderId()).size() <= 1) {
            throw ApiErrors.conflict("This is the last item of the order. Cancel the order instead.");
        }
        changeStock(item.getProduct(), item.getQuantity());
        orderItemRepository.delete(item);
        updateOrderTotal(order);
        return "Order item deleted successfully";
    }

    // ------------------------------------------------------------------ helpers

    private static int validQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw ApiErrors.badRequest("Quantity must be at least 1.");
        }
        if (quantity > MAX_QUANTITY) {
            throw ApiErrors.badRequest("Quantity cannot be more than " + MAX_QUANTITY + ".");
        }
        return quantity;
    }

    /** Lines may only change while the order is still being prepared and nothing has been paid. */
    private static void requireEditable(Order order) {
        String status = order.getOrderStatus() == null ? "PENDING" : order.getOrderStatus().toUpperCase();
        if (!"PENDING".equals(status) && !"CONFIRMED".equals(status)) {
            throw ApiErrors.conflict("Items can only be changed while the order is PENDING or CONFIRMED. This order is "
                    + status + ".");
        }
        if ("PAID".equalsIgnoreCase(order.getPaymentStatus())) {
            throw ApiErrors.conflict("This order has already been paid, so its items cannot be changed.");
        }
    }

    /** delta < 0 reserves units, delta > 0 puts them back. Untracked products are ignored. */
    private void changeStock(Product product, int delta) {
        Inventory inventory = inventoryRepository.findForUpdate(product.getProductId()).orElse(null);
        if (inventory == null) {
            return;
        }
        int available = inventory.getQuantityAvailable() == null ? 0 : inventory.getQuantityAvailable();
        if (delta < 0 && available < -delta) {
            throw ApiErrors.conflict(available <= 0
                    ? "'" + product.getName() + "' is out of stock."
                    : "Only " + available + " unit(s) of '" + product.getName() + "' are available.");
        }
        inventory.setQuantityAvailable(available + delta);
        inventory.setStockStatus(StockRules.statusFor(inventory.getQuantityAvailable(), inventory.getReorderLevel()));
        inventoryRepository.save(inventory);
    }

    private void updateOrderTotal(Order order) {
        double total = orderItemRepository.findByOrder_OrderId(order.getOrderId()).stream()
                .mapToDouble(OrderItem::getSubtotal)
                .sum();
        order.setTotalAmount(Math.round(total * 100.0) / 100.0);
        orderRepository.save(order);
    }

    private OrderItemResponse convertToResponse(OrderItem item) {
        return new OrderItemResponse(
                item.getOrderItemId(),
                item.getOrder().getOrderId(),
                item.getProduct().getProductId(),
                item.getProduct().getName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getSubtotal()
        );
    }
}
