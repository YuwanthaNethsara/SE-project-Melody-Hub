package com.example.project1.service;

import com.example.project1.dto.OrderItemRequest;
import com.example.project1.dto.OrderRequest;
import com.example.project1.dto.OrderResponse;
import com.example.project1.entity.Cart;
import com.example.project1.entity.CartItem;
import com.example.project1.entity.Inventory;
import com.example.project1.entity.Order;
import com.example.project1.entity.OrderItem;
import com.example.project1.entity.Product;
import com.example.project1.entity.User;
import com.example.project1.repository.CartItemRepository;
import com.example.project1.repository.CartRepository;
import com.example.project1.repository.InventoryRepository;
import com.example.project1.pattern.state.OrderState;
import com.example.project1.pattern.state.OrderStates;
import com.example.project1.repository.OrderItemRepository;
import com.example.project1.repository.OrderRepository;
import com.example.project1.repository.PaymentRepository;
import com.example.project1.repository.ProductRepository;
import com.example.project1.repository.UserRepository;
import com.example.project1.repository.WarrantyClaimRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    // The order lifecycle (PENDING -> CONFIRMED -> PROCESSING -> SHIPPED -> DELIVERED, and
    // CANCELLED until it has shipped) now lives in the STATE pattern: see pattern/state.

    private static final Set<String> PAYMENT_STATUSES = Set.of("PENDING", "PAID", "FAILED", "REFUNDED");

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final PaymentRepository paymentRepository;
    private final InventoryRepository inventoryRepository;
    private final PricingService pricingService;
    private final WarrantyClaimRepository warrantyClaimRepository;

    public OrderServiceImpl(OrderRepository orderRepository,
                            OrderItemRepository orderItemRepository,
                            UserRepository userRepository,
                            ProductRepository productRepository,
                            CartRepository cartRepository,
                            CartItemRepository cartItemRepository,
                            PaymentRepository paymentRepository,
                            InventoryRepository inventoryRepository,
                            PricingService pricingService,
                            WarrantyClaimRepository warrantyClaimRepository) {
        this.warrantyClaimRepository = warrantyClaimRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.paymentRepository = paymentRepository;
        this.inventoryRepository = inventoryRepository;
        this.pricingService = pricingService;
    }

    // =====================================================
    // CREATE ORDER (ADMIN on behalf of a customer)
    // =====================================================
    @Override
    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        if (request.getUserId() == null) throw new IllegalArgumentException("userId is required for admin order creation");
        if (request.getShippingAddress() == null || request.getShippingAddress().isBlank()) {
            throw new IllegalArgumentException("Shipping address is required");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("At least one order item is required");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Order order = newPendingOrder(user, request.getShippingAddress().trim());
        Order saved = orderRepository.save(order);

        double total = 0.0;
        for (OrderItemRequest itemRequest : request.getItems()) {
            total += addItemToOrder(saved, itemRequest.getProductId(), itemRequest.getQuantity());
        }

        saved.setTotalAmount(round2(total));
        orderRepository.save(saved);

        return convertToResponse(saved);
    }

    // =====================================================
    // CHECKOUT (customer, from their own cart)
    // The backend recalculates every price and the total.
    // =====================================================
    @Override
    @Transactional
    public OrderResponse checkoutCart(String email, String shippingAddress) {
        if (shippingAddress == null || shippingAddress.isBlank()) {
            throw new IllegalArgumentException("Shipping address is required");
        }
        if (shippingAddress.trim().length() < 8) {
            throw new IllegalArgumentException("Please enter a complete shipping address");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = cartRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new IllegalStateException("Your cart is empty"));

        List<CartItem> cartItems = cartItemRepository.findByCart_CartId(cart.getCartId());
        if (cartItems.isEmpty()) throw new IllegalStateException("Your cart is empty");

        // lock stock rows in a fixed order so concurrent checkouts cannot deadlock
        cartItems.sort(Comparator.comparing(ci -> ci.getProduct().getProductId()));

        Order savedOrder = orderRepository.save(newPendingOrder(user, shippingAddress.trim()));

        double total = 0.0;
        for (CartItem cartItem : cartItems) {
            total += addItemToOrder(savedOrder, cartItem.getProduct().getProductId(), cartItem.getQuantity());
        }

        savedOrder.setTotalAmount(round2(total));
        orderRepository.save(savedOrder);

        cartItemRepository.deleteAll(cartItems);

        return convertToResponse(savedOrder);
    }

    private Order newPendingOrder(User user, String shippingAddress) {
        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setTotalAmount(0.0);
        order.setOrderStatus("PENDING");
        order.setPaymentStatus("PENDING");
        order.setShippingAddress(shippingAddress);
        return order;
    }

    /** Validates, reserves stock, saves the line and returns its subtotal. */
    private double addItemToOrder(Order order, Long productId, Integer quantity) {
        if (productId == null || quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Each order item needs a valid productId and positive quantity");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

        StockRules.requireSellable(product);
        reserveStock(product, quantity);

        BigDecimal unitPrice = pricingService.unitPrice(product);
        double subtotal = pricingService.lineTotal(unitPrice, quantity);

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProduct(product);
        item.setQuantity(quantity);
        item.setUnitPrice(unitPrice.doubleValue());
        item.setSubtotal(subtotal);
        orderItemRepository.save(item);

        return subtotal;
    }

    // =====================================================
    // STOCK
    // If a product has no inventory record its stock is not tracked and it can be sold.
    // =====================================================
    private void reserveStock(Product product, int quantity) {
        Inventory inventory = inventoryRepository.findForUpdate(product.getProductId()).orElse(null);
        if (inventory == null) return;

        int available = inventory.getQuantityAvailable() == null ? 0 : inventory.getQuantityAvailable();
        if (available < quantity) {
            throw new IllegalStateException(available <= 0
                    ? "'" + product.getName() + "' is out of stock"
                    : "Only " + available + " unit(s) of '" + product.getName() + "' are available");
        }
        inventory.setQuantityAvailable(available - quantity);
        inventory.setStockStatus(StockRules.statusFor(inventory.getQuantityAvailable(), inventory.getReorderLevel()));
        inventoryRepository.save(inventory);
    }

    private void restoreStock(Order order) {
        for (OrderItem item : orderItemRepository.findByOrder_OrderId(order.getOrderId())) {
            Inventory inventory = inventoryRepository
                    .findForUpdate(item.getProduct().getProductId()).orElse(null);
            if (inventory == null) continue;
            int current = inventory.getQuantityAvailable() == null ? 0 : inventory.getQuantityAvailable();
            inventory.setQuantityAvailable(current + item.getQuantity());
            inventory.setStockStatus(StockRules.statusFor(inventory.getQuantityAvailable(), inventory.getReorderLevel()));
            inventoryRepository.save(inventory);
        }
    }

    // =====================================================
    // READ
    // =====================================================
    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        return orderRepository.findById(id).map(this::convertToResponse).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUser(Long userId) {
        return orderRepository.findByUser_Id(userId).stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByStatus(String status) {
        return orderRepository.findByOrderStatus(status).stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByPaymentStatus(String status) {
        return orderRepository.findByPaymentStatus(status).stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    // =====================================================
    // STATUS CHANGES
    // =====================================================
    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long id, String status) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order == null) return null;

        String newStatus = normalize(status);
        if (!OrderStates.isKnown(newStatus)) {
            throw new IllegalArgumentException("Unknown order status: " + status);
        }

        OrderState current = order.currentState();
        if (current.name().equals(newStatus)) {
            return convertToResponse(order);
        }

        // STATE pattern: the current state decides whether this move is allowed
        current.transitionTo(order, newStatus);

        if ("CANCELLED".equals(newStatus)) {
            applyCancellationEffects(order);
        }

        return convertToResponse(orderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderResponse updatePaymentStatus(Long id, String status) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order == null) return null;

        String newStatus = normalize(status);
        if (!PAYMENT_STATUSES.contains(newStatus)) {
            throw new IllegalArgumentException("Unknown payment status: " + status);
        }

        order.setPaymentStatus(newStatus);
        return convertToResponse(orderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long id, String requesterEmail, boolean staff) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!staff && !order.getUser().getEmail().equalsIgnoreCase(requesterEmail)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot access another user's order");
        }

        String current = order.currentState().name();

        if ("CANCELLED".equals(current)) {
            throw new IllegalStateException("This order is already cancelled");
        }

        // customers may only cancel before the shop has started working on it
        if (!staff && !"PENDING".equals(current)) {
            throw new IllegalStateException(
                    "Only orders that are still PENDING can be cancelled. Please contact support.");
        }

        // STATE pattern: only states that allow it (PENDING, CONFIRMED, PROCESSING) let the order cancel
        order.cancel();

        applyCancellationEffects(order);
        return convertToResponse(orderRepository.save(order));
    }

    /** Side effects of a cancellation. The status itself was already set by CancelledState. */
    private void applyCancellationEffects(Order order) {
        restoreStock(order);

        paymentRepository.findByOrder_OrderId(order.getOrderId()).ifPresent(payment -> {
            if ("PAID".equalsIgnoreCase(payment.getPaymentStatus())) {
                payment.setPaymentStatus("REFUNDED");
                paymentRepository.save(payment);
                order.setPaymentStatus("REFUNDED");
            }
        });
    }

    // =====================================================
    // CUSTOMER / ADMIN: change the shipping address before the order is processed
    // =====================================================
    @Override
    @Transactional
    public OrderResponse updateShippingAddress(Long id, String shippingAddress,
                                               String requesterEmail, boolean staff) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!staff && !order.getUser().getEmail().equalsIgnoreCase(requesterEmail)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot access another user's order");
        }

        String state = order.currentState().name();

        // the shop has not started working on the order yet
        if (!"PENDING".equals(state) && !"CONFIRMED".equals(state)) {
            throw new IllegalStateException(
                    "The shipping address can only be changed before the order is processed. "
                            + "This order is already " + state + ".");
        }

        String address = shippingAddress == null ? "" : shippingAddress.trim();

        if (address.isEmpty()) {
            throw new IllegalArgumentException("Shipping address is required");
        }
        if (address.length() < 5) {
            throw new IllegalArgumentException("Shipping address must be at least 5 characters");
        }
        if (address.length() > 300) {
            throw new IllegalArgumentException("Shipping address must be at most 300 characters");
        }

        order.setShippingAddress(address);

        return convertToResponse(orderRepository.save(order));
    }

    @Override
    @Transactional
    public String deleteOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // warranty claims point at the order's items, so the order must stay while a claim exists
        if (warrantyClaimRepository.existsByOrderItem_Order_OrderId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Warranty claims exist for this order, so it cannot be deleted.");
        }

        String current = order.getOrderStatus() == null ? "" : order.getOrderStatus().toUpperCase();
        if (!"CANCELLED".equals(current) && !"DELIVERED".equals(current)) {
            restoreStock(order); // the reserved units go back on the shelf
        }

        paymentRepository.findByOrder_OrderId(id).ifPresent(paymentRepository::delete);
        orderItemRepository.deleteAll(orderItemRepository.findByOrder_OrderId(id));
        orderRepository.delete(order);
        return "Order deleted successfully";
    }

    // =====================================================
    // helpers
    // =====================================================
    private static String normalize(String status) {
        return status == null ? "" : status.trim().toUpperCase();
    }

    private static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private OrderResponse convertToResponse(Order order) {
        User user = order.getUser();
        return new OrderResponse(
                order.getOrderId(), user.getId(), user.getName(), user.getEmail(),
                order.getOrderDate(), order.getTotalAmount(), order.getOrderStatus(),
                order.getPaymentStatus(), order.getShippingAddress());
    }
}
