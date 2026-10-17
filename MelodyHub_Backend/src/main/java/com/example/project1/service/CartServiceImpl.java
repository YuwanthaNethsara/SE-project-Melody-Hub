package com.example.project1.service;

import com.example.project1.dto.CartItemRequest;
import com.example.project1.dto.CartItemResponse;
import com.example.project1.dto.CartResponse;
import com.example.project1.entity.Cart;
import com.example.project1.entity.CartItem;
import com.example.project1.entity.Inventory;
import com.example.project1.entity.Product;
import com.example.project1.entity.User;
import com.example.project1.repository.CartItemRepository;
import com.example.project1.repository.CartRepository;
import com.example.project1.repository.InventoryRepository;
import com.example.project1.repository.ProductRepository;
import com.example.project1.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    private static final int MAX_QUANTITY_PER_ITEM = 99;

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final PricingService pricingService;

    public CartServiceImpl(CartRepository cartRepository,
                           CartItemRepository cartItemRepository,
                           UserRepository userRepository,
                           ProductRepository productRepository,
                           InventoryRepository inventoryRepository,
                           PricingService pricingService) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.pricingService = pricingService;
    }

    private User user(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUser_Id(user.getId()).orElseGet(() -> {
            Cart cart = new Cart();
            cart.setUser(user);
            return cartRepository.save(cart);
        });
    }

    /** Product must be sellable and (if stock is tracked) enough units must exist. */
    private void checkAvailability(Product product, int wantedQuantity) {
        StockRules.requireSellable(product);

        if (wantedQuantity > MAX_QUANTITY_PER_ITEM) {
            throw new IllegalArgumentException(
                    "You can order at most " + MAX_QUANTITY_PER_ITEM + " units of one product");
        }

        Inventory inventory = inventoryRepository
                .findByProduct_ProductId(product.getProductId())
                .orElse(null);

        if (inventory != null) {
            int available = inventory.getQuantityAvailable() == null ? 0 : inventory.getQuantityAvailable();
            if (available <= 0) {
                throw new IllegalStateException("'" + product.getName() + "' is out of stock");
            }
            if (wantedQuantity > available) {
                throw new IllegalStateException(
                        "Only " + available + " unit(s) of '" + product.getName() + "' are available");
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponse getMyCart(String email) {
        User user = user(email);
        Cart cart = cartRepository.findByUser_Id(user.getId()).orElse(null);
        if (cart == null) return new CartResponse(null, user.getId(), List.of(), 0.0);
        return toResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse addItem(String email, CartItemRequest request) {
        if (request.getProductId() == null || request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new IllegalArgumentException("Product and positive quantity are required");
        }
        User user = user(email);
        Cart cart = getOrCreateCart(user);
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found."));

        CartItem item = cartItemRepository
                .findByCart_CartIdAndProduct_ProductId(cart.getCartId(), product.getProductId())
                .orElseGet(() -> {
                    CartItem newItem = new CartItem();
                    newItem.setCart(cart);
                    newItem.setProduct(product);
                    newItem.setQuantity(0);
                    return newItem;
                });

        int newQuantity = item.getQuantity() + request.getQuantity();
        checkAvailability(product, newQuantity);

        item.setQuantity(newQuantity);
        cartItemRepository.save(item);
        return toResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse updateItem(String email, Long cartItemId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        User user = user(email);
        Cart cart = getOrCreateCart(user);
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "This item is no longer in your cart."));
        if (!item.getCart().getCartId().equals(cart.getCartId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot modify another user's cart");
        }

        checkAvailability(item.getProduct(), quantity);

        item.setQuantity(quantity);
        cartItemRepository.save(item);
        return toResponse(cart);
    }

    @Override
    @Transactional
    public String removeItem(String email, Long cartItemId) {
        User user = user(email);
        Cart cart = cartRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Your cart is empty."));
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "This item is no longer in your cart."));
        if (!item.getCart().getCartId().equals(cart.getCartId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot modify another user's cart");
        }
        cartItemRepository.delete(item);
        return "Cart item removed successfully";
    }

    @Override
    @Transactional
    public String clearCart(String email) {
        User user = user(email);
        Cart cart = cartRepository.findByUser_Id(user.getId()).orElse(null);
        if (cart == null) return "Cart is already empty";
        cartItemRepository.deleteAll(cartItemRepository.findByCart_CartId(cart.getCartId()));
        return "Cart cleared successfully";
    }

    private CartResponse toResponse(Cart cart) {
        List<CartItemResponse> items = cartItemRepository.findByCart_CartId(cart.getCartId())
                .stream()
                .map(item -> {
                    BigDecimal unitPrice = pricingService.unitPrice(item.getProduct());
                    CartItemResponse response = new CartItemResponse(
                            item.getCartItemId(),
                            item.getProduct().getProductId(),
                            item.getProduct().getName(),
                            unitPrice.doubleValue(),
                            item.getQuantity(),
                            pricingService.lineTotal(unitPrice, item.getQuantity()));
                    response.setImageUrl(item.getProduct().getImageUrl());
                    response.setOriginalPrice(item.getProduct().getPrice() == null
                            ? null : item.getProduct().getPrice().doubleValue());
                    inventoryRepository.findByProduct_ProductId(item.getProduct().getProductId())
                            .ifPresent(inv -> response.setQuantityAvailable(inv.getQuantityAvailable()));
                    return response;
                })
                .collect(Collectors.toList());
        double total = items.stream().mapToDouble(CartItemResponse::getSubtotal).sum();
        total = Math.round(total * 100.0) / 100.0;
        return new CartResponse(cart.getCartId(), cart.getUser().getId(), items, total);
    }
}
