package com.example.project1.service;

import com.example.project1.exception.ApiErrors;
import com.example.project1.storage.FileStorageService;
import org.springframework.web.multipart.MultipartFile;

import com.example.project1.dto.ProductRequest;
import com.example.project1.dto.ProductResponse;
import com.example.project1.entity.Category;
import com.example.project1.entity.Product;
import com.example.project1.repository.CategoryRepository;
import com.example.project1.repository.InventoryRepository;
import com.example.project1.repository.CartItemRepository;
import com.example.project1.repository.FeaturedProductRepository;
import com.example.project1.repository.OrderItemRepository;
import com.example.project1.repository.PhysicalProductRepository;
import com.example.project1.repository.PromotionRepository;
import com.example.project1.repository.PurchaseOrderRepository;
import com.example.project1.repository.WarrantyRepository;
import com.example.project1.repository.ReviewRepository;
import com.example.project1.pattern.factory.ProductCreatorProvider;
import com.example.project1.repository.ProductRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    private static final java.util.Set<String> PRODUCT_STATUSES = java.util.Set.of(
            "ACTIVE", "INACTIVE", "DISCONTINUED", "OUT_OF_STOCK", "UNAVAILABLE", "ARCHIVED");

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final InventoryRepository inventoryRepository;
    private final ProductCreatorProvider creatorProvider; // Factory Method: decides which Product is built

    // used to refuse / tidy up a delete safely
    private final OrderItemRepository orderItemRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final CartItemRepository cartItemRepository;
    private final PhysicalProductRepository physicalProductRepository;
    private final PromotionRepository promotionRepository;
    private final FeaturedProductRepository featuredProductRepository;
    private final WarrantyRepository warrantyRepository;
    private final ReviewRepository reviewRepository;
    private final FileStorageService storage;

    public ProductServiceImpl(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            InventoryRepository inventoryRepository,
            ProductCreatorProvider creatorProvider,
            OrderItemRepository orderItemRepository,
            PurchaseOrderRepository purchaseOrderRepository,
            CartItemRepository cartItemRepository,
            PhysicalProductRepository physicalProductRepository,
            PromotionRepository promotionRepository,
            FeaturedProductRepository featuredProductRepository,
            WarrantyRepository warrantyRepository,
            ReviewRepository reviewRepository,
            FileStorageService storage) {
        this.reviewRepository = reviewRepository;
        this.storage = storage;
        this.orderItemRepository = orderItemRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.cartItemRepository = cartItemRepository;
        this.physicalProductRepository = physicalProductRepository;
        this.promotionRepository = promotionRepository;
        this.featuredProductRepository = featuredProductRepository;
        this.warrantyRepository = warrantyRepository;
        this.inventoryRepository = inventoryRepository;
        this.creatorProvider = creatorProvider;

        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public ProductResponse createProduct(ProductRequest request) {
        validateRequest(request, null);
        String imageUrl = storage.resolveJsonImageUrl(null, request.getImageUrl(),
                Boolean.TRUE.equals(request.getRemoveImage()), 255);
        Category category = findCategory(request.getCategoryId());
        // FACTORY METHOD: the creator for the requested type builds the Product
        Product product = creatorProvider.forType(request.getProductType()).create(request, category);
        product.setImageUrl(imageUrl);
        return convertToResponse(productRepository.save(product));
    }

    @Override
    public ProductResponse createProduct(ProductRequest request, MultipartFile image) {
        validateRequest(request, null);
        Category category = findCategory(request.getCategoryId());
        return storage.replaceImage(null, image, false, FileStorageService.PRODUCTS, url -> {
            // FACTORY METHOD: the creator for the requested type builds the Product
            Product product = creatorProvider.forType(request.getProductType()).create(request, category);
            product.setImageUrl(url);
            return convertToResponse(productRepository.save(product));
        });
    }

    @Override
    public List<ProductResponse> getAllProducts() {

        return productRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ProductResponse getProductById(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        return convertToResponse(product);
    }

    @Override
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = findProduct(id);
        validateRequest(request, id);
        String oldUrl = product.getImageUrl();
        String newUrl = storage.resolveJsonImageUrl(oldUrl, request.getImageUrl(),
                Boolean.TRUE.equals(request.getRemoveImage()), 255);
        Category category = findCategory(request.getCategoryId());
        creatorProvider.forType(request.getProductType()).populate(product, request, category);
        product.setImageUrl(newUrl);
        ProductResponse saved = convertToResponse(productRepository.save(product));
        if (!java.util.Objects.equals(oldUrl, newUrl)) {
            storage.deleteImageQuietly(oldUrl);
        }
        return saved;
    }

    @Override
    public ProductResponse updateProduct(Long id, ProductRequest request, MultipartFile image) {
        Product product = findProduct(id);
        validateRequest(request, id);
        Category category = findCategory(request.getCategoryId());
        String oldUrl = product.getImageUrl();
        return storage.replaceImage(oldUrl, image, Boolean.TRUE.equals(request.getRemoveImage()),
                FileStorageService.PRODUCTS, url -> {
                    creatorProvider.forType(request.getProductType()).populate(product, request, category);
                    product.setImageUrl(url);
                    return convertToResponse(productRepository.save(product));
                });
    }

    private Product findProduct(Long id) {
        return productRepository.findById(ApiErrors.required(id, "Product id is required."))
                .orElseThrow(() -> ApiErrors.notFound("Product not found."));
    }

    private Category findCategory(Long categoryId) {
        return categoryRepository.findById(ApiErrors.required(categoryId, "Please choose a category."))
                .orElseThrow(() -> ApiErrors.notFound("The selected category was not found."));
    }

    @Override
    @Transactional
    public String deleteProduct(Long id) {

        Product product = findProduct(id);

        // history must never be lost: products that were sold or ordered from a supplier stay
        if (orderItemRepository.existsByProduct_ProductId(id)
                || purchaseOrderRepository.existsByProduct_ProductId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This product is part of customer orders or purchase orders and cannot be deleted. "
                            + "Set its status to INACTIVE to hide it from the store instead.");
        }

        // everything that only exists for this product goes with it
        cartItemRepository.deleteAll(cartItemRepository.findByProduct_ProductId(id));
        promotionRepository.deleteAll(promotionRepository.findByProduct_ProductId(id));
        featuredProductRepository.findByProduct_ProductId(id).ifPresent(featuredProductRepository::delete);
        warrantyRepository.findByProduct_ProductId(id).ifPresent(warrantyRepository::delete);
        inventoryRepository.findByProduct_ProductId(id).ifPresent(inventoryRepository::delete);
        reviewRepository.deleteAll(reviewRepository.findByProduct_ProductId(id));
        if (physicalProductRepository.existsById(id)) {
            physicalProductRepository.deleteById(id);
        }

        String imageUrl = product.getImageUrl();
        productRepository.delete(product);
        storage.deleteImageQuietly(imageUrl); // no orphan picture left in uploads/products

        return "Product deleted successfully";
    }

    // SKU rules (the other rules are Bean Validation annotations on ProductRequest)
    private void validateRequest(ProductRequest request, Long currentId) {

        if (request.getName() != null && request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is required");
        }

        String sku = request.getSku() == null ? "" : request.getSku().trim();

        if (sku.isEmpty()) {
            return; // the SKU is optional
        }

        if (!sku.matches("^[A-Za-z0-9][A-Za-z0-9._-]{1,49}$")) {
            throw new IllegalArgumentException(
                    "SKU must be 2-50 characters: letters, numbers, dots, dashes or underscores");
        }

        productRepository.findBySku(sku.toUpperCase()).ifPresent(other -> {
            if (currentId == null || !other.getProductId().equals(currentId)) {
                throw ApiErrors.conflict("A product with this SKU already exists.");
            }
        });
    }

    @Override
    public List<ProductResponse> searchProducts(String name) {

        return productRepository
                .findByNameContainingIgnoreCase(name)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> getProductsByCategory(Long categoryId) {

        return productRepository
                .findByCategory_CategoryId(categoryId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private ProductResponse convertToResponse(Product product) {
        ProductResponse response = new ProductResponse(
                product.getProductId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getProductType(),
                product.getStatus(),
                product.getImageUrl(),
                product.getCreatedAt(),
                product.getUpdatedAt(),
                product.getCategory().getCategoryId(),
                product.getCategory().getCategoryName()
        );

        response.setSku(product.getSku());

        inventoryRepository.findByProduct_ProductId(product.getProductId())
                .ifPresent(inventory -> {
                    response.setQuantityAvailable(inventory.getQuantityAvailable());
                    response.setStockStatus(inventory.getStockStatus() == null
                            ? null : inventory.getStockStatus().name());
                });

        return response;
    }
}
