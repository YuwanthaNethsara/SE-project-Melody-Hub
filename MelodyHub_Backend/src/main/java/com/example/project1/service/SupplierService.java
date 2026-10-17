package com.example.project1.service;

import com.example.project1.dto.SupplierImportRequest;
import com.example.project1.dto.SupplierImportResponse;
import com.example.project1.dto.SupplierRequest;
import com.example.project1.dto.SupplierResponse;

import java.util.List;
import java.util.Map;

public interface SupplierService {

    SupplierResponse createSupplier(
            SupplierRequest request
    );

    List<SupplierResponse> getAllSuppliers();

    SupplierResponse getSupplierById(Long id);

    List<SupplierResponse> getSuppliersByStatus(
            String status
    );

    List<SupplierResponse> searchSuppliers(
            String name
    );

    SupplierResponse updateSupplier(
            Long id,
            SupplierRequest request
    );

    String deleteSupplier(Long id);

    // ADAPTER pattern: bulk-add suppliers from data in another system's format
    SupplierImportResponse importSuppliers(
            SupplierImportRequest request
    );

    Map<String, String> getSupportedImportFormats();
}
