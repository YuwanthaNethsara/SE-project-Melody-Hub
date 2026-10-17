package com.example.project1.service;

import com.example.project1.dto.SupplierContractRequest;
import com.example.project1.dto.SupplierContractResponse;

import java.util.List;

public interface SupplierContractService {

    SupplierContractResponse createContract(
            SupplierContractRequest request
    );

    List<SupplierContractResponse> getAllContracts();

    SupplierContractResponse getContractById(
            Long id
    );

    List<SupplierContractResponse> getContractsBySupplier(
            Long supplierId
    );

    List<SupplierContractResponse> getContractsByStatus(
            String status
    );

    List<SupplierContractResponse> searchContracts(
            String name
    );

    SupplierContractResponse updateContract(
            Long id,
            SupplierContractRequest request
    );

    String deleteContract(Long id);
}
