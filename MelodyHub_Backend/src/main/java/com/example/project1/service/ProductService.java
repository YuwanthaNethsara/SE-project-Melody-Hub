package com.example.project1.service;

import com.example.project1.dto.ProductRequest;
import com.example.project1.dto.ProductResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {

    ProductResponse createProduct(ProductRequest request);

    /** Multipart variant: the product picture is uploaded together with the data. */
    ProductResponse createProduct(ProductRequest request, MultipartFile image);

    List<ProductResponse> getAllProducts();

    ProductResponse getProductById(Long id);

    ProductResponse updateProduct(Long id, ProductRequest request);

    ProductResponse updateProduct(Long id, ProductRequest request, MultipartFile image);

    String deleteProduct(Long id);

    List<ProductResponse> searchProducts(String name);

    List<ProductResponse> getProductsByCategory(Long categoryId);
}
