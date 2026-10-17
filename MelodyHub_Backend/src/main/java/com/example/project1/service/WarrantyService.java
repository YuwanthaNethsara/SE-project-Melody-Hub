package com.example.project1.service;

import com.example.project1.dto.WarrantyRequest;
import com.example.project1.dto.WarrantyResponse;

import java.util.List;

public interface WarrantyService {

    WarrantyResponse createWarranty(WarrantyRequest request);

    List<WarrantyResponse> getAllWarranties();

    WarrantyResponse getWarrantyById(Long id);

    /** null when the product has no warranty */
    WarrantyResponse findWarrantyByProduct(Long productId);

    WarrantyResponse getWarrantyByProduct(Long productId);

    WarrantyResponse updateWarranty(
            Long id,
            WarrantyRequest request
    );

    String deleteWarranty(Long id);
}
