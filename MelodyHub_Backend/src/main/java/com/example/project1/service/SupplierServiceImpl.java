package com.example.project1.service;

import com.example.project1.dto.SupplierImportRequest;
import com.example.project1.dto.SupplierImportResponse;
import com.example.project1.dto.SupplierRequest;
import com.example.project1.dto.SupplierResponse;
import com.example.project1.entity.Supplier;
import com.example.project1.pattern.adapter.SupplierFeedAdapter;
import com.example.project1.pattern.adapter.SupplierFeedAdapterRegistry;
import com.example.project1.repository.SupplierRepository;

import com.example.project1.repository.SupplierContractRepository;
import com.example.project1.repository.PurchaseOrderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SupplierServiceImpl
        implements SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierContractRepository supplierContractRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;

    // Adapter pattern: converts a supplier system's own data format into SupplierRequest
    private final SupplierFeedAdapterRegistry feedAdapters;

    public SupplierServiceImpl(
            SupplierRepository supplierRepository,
            SupplierFeedAdapterRegistry feedAdapters,
            SupplierContractRepository supplierContractRepository,
            PurchaseOrderRepository purchaseOrderRepository
    ) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.supplierContractRepository = supplierContractRepository;
        this.supplierRepository = supplierRepository;
        this.feedAdapters = feedAdapters;
    }

    // ==========================================
    // CREATE
    // ==========================================

    @Override
    public SupplierResponse createSupplier(
            SupplierRequest request
    ) {

        validateRequest(request);

        if (supplierRepository.existsByEmail(
                request.getEmail()
        )) {

            throw new RuntimeException(
                    "Supplier email already exists"
            );
        }

        Supplier supplier = new Supplier();

        supplier.setSupplierName(
                request.getSupplierName()
        );

        supplier.setContactPerson(
                request.getContactPerson()
        );

        supplier.setEmail(
                request.getEmail()
        );

        supplier.setPhone(
                request.getPhone()
        );

        supplier.setAddress(
                request.getAddress()
        );

        if (request.getStatus() == null ||
                request.getStatus().trim().isEmpty()) {

            supplier.setStatus("ACTIVE");

        } else {

            supplier.setStatus(
                    request.getStatus().toUpperCase()
            );
        }

        supplier.setCreatedDate(
                LocalDateTime.now()
        );

        Supplier saved =
                supplierRepository.save(supplier);

        return convertToResponse(saved);
    }

    // ==========================================
    // GET ALL
    // ==========================================

    @Override
    public List<SupplierResponse> getAllSuppliers() {

        return supplierRepository
                .findAll()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // ==========================================
    // GET BY ID
    // ==========================================

    @Override
    public SupplierResponse getSupplierById(
            Long id
    ) {

        Supplier supplier =
                supplierRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Supplier not found"
                                )
                        );

        return convertToResponse(supplier);
    }

    // ==========================================
    // GET BY STATUS
    // ==========================================

    @Override
    public List<SupplierResponse> getSuppliersByStatus(
            String status
    ) {

        return supplierRepository
                .findByStatus(status.toUpperCase())
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // ==========================================
    // SEARCH
    // ==========================================

    @Override
    public List<SupplierResponse> searchSuppliers(
            String name
    ) {

        return supplierRepository
                .findBySupplierNameContainingIgnoreCase(name)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // ==========================================
    // UPDATE
    // ==========================================

    @Override
    public SupplierResponse updateSupplier(
            Long id,
            SupplierRequest request
    ) {

        validateRequest(request);

        Supplier supplier =
                supplierRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Supplier not found"
                                )
                        );

        if (!supplier.getEmail()
                .equalsIgnoreCase(request.getEmail())
                && supplierRepository.existsByEmail(
                request.getEmail()
        )) {

            throw new RuntimeException(
                    "Supplier email already exists"
            );
        }

        supplier.setSupplierName(
                request.getSupplierName()
        );

        supplier.setContactPerson(
                request.getContactPerson()
        );

        supplier.setEmail(
                request.getEmail()
        );

        supplier.setPhone(
                request.getPhone()
        );

        supplier.setAddress(
                request.getAddress()
        );

        if (request.getStatus() != null &&
                !request.getStatus().trim().isEmpty()) {

            supplier.setStatus(
                    request.getStatus().toUpperCase()
            );
        }

        Supplier updated =
                supplierRepository.save(supplier);

        return convertToResponse(updated);
    }

    // ==========================================
    // DELETE
    // ==========================================

    @Override
    public String deleteSupplier(Long id) {

        if (!supplierRepository.existsById(id)) {

            throw new RuntimeException(
                    "Supplier not found"
            );
        }

        if (supplierContractRepository.existsBySupplier_SupplierId(id)
                || purchaseOrderRepository.existsBySupplier_SupplierId(id)) {

            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This supplier still has contracts or purchase orders. "
                            + "Delete those first, or set the supplier to INACTIVE instead.");
        }

        supplierRepository.deleteById(id);

        return "Supplier deleted successfully";
    }

    // ==========================================
    // IMPORT (ADAPTER pattern)
    // The adapter turns CSV / XML from another system into
    // SupplierRequest objects; each one then goes through the SAME
    // createSupplier() as a supplier typed in by hand.
    // ==========================================

    @Override
    public SupplierImportResponse importSuppliers(
            SupplierImportRequest request
    ) {

        if (request.getData() == null ||
                request.getData().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Supplier data is required"
            );
        }

        SupplierFeedAdapter adapter =
                feedAdapters.forFormat(request.getFormat());

        List<SupplierRequest> requests =
                adapter.toSupplierRequests(request.getData());

        List<SupplierResponse> imported = new ArrayList<>();
        List<String> skipped = new ArrayList<>();

        for (int i = 0; i < requests.size(); i++) {

            SupplierRequest supplierRequest = requests.get(i);

            try {

                imported.add(createSupplier(supplierRequest));

            } catch (RuntimeException e) {

                String name = supplierRequest.getSupplierName() == null
                        ? "supplier " + (i + 1)
                        : supplierRequest.getSupplierName();

                skipped.add(name + ": " + e.getMessage());
            }
        }

        return new SupplierImportResponse(
                adapter.getFormat(),
                requests.size(),
                imported.size(),
                imported,
                skipped
        );
    }

    @Override
    public Map<String, String> getSupportedImportFormats() {

        return feedAdapters.describeAll();
    }

    // ==========================================
    // VALIDATION
    // ==========================================

    private void validateRequest(
            SupplierRequest request
    ) {

        if (request.getSupplierName() == null ||
                request.getSupplierName().trim().isEmpty()) {

            throw new RuntimeException(
                    "Supplier name is required"
            );
        }

        if (request.getContactPerson() == null ||
                request.getContactPerson().trim().isEmpty()) {

            throw new RuntimeException(
                    "Contact person is required"
            );
        }

        if (request.getEmail() == null ||
                request.getEmail().trim().isEmpty()) {

            throw new RuntimeException(
                    "Email is required"
            );
        }

        if (request.getPhone() == null ||
                request.getPhone().trim().isEmpty()) {

            throw new RuntimeException(
                    "Phone is required"
            );
        }

        if (request.getStatus() != null &&
                !request.getStatus().trim().isEmpty()) {

            String status =
                    request.getStatus().toUpperCase();

            if (!status.equals("ACTIVE") &&
                    !status.equals("INACTIVE")) {

                throw new RuntimeException(
                        "Status must be ACTIVE or INACTIVE"
                );
            }
        }
    }

    // ==========================================
    // CONVERT
    // ==========================================

    private SupplierResponse convertToResponse(
            Supplier supplier
    ) {

        return new SupplierResponse(

                supplier.getSupplierId(),

                supplier.getSupplierName(),

                supplier.getContactPerson(),

                supplier.getEmail(),

                supplier.getPhone(),

                supplier.getAddress(),

                supplier.getStatus(),

                supplier.getCreatedDate()
        );
    }
}
