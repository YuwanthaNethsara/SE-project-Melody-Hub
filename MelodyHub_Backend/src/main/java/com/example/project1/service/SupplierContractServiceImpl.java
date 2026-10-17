package com.example.project1.service;

import com.example.project1.dto.SupplierContractRequest;
import com.example.project1.dto.SupplierContractResponse;
import com.example.project1.entity.Supplier;
import com.example.project1.entity.SupplierContract;
import com.example.project1.repository.SupplierContractRepository;
import com.example.project1.repository.SupplierRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SupplierContractServiceImpl
        implements SupplierContractService {

    private final SupplierContractRepository contractRepository;
    private final SupplierRepository supplierRepository;

    public SupplierContractServiceImpl(
            SupplierContractRepository contractRepository,
            SupplierRepository supplierRepository
    ) {
        this.contractRepository = contractRepository;
        this.supplierRepository = supplierRepository;
    }

    // CREATE
    @Override
    public SupplierContractResponse createContract(
            SupplierContractRequest request
    ) {

        validateRequest(request);

        Supplier supplier =
                supplierRepository.findById(
                        request.getSupplierId()
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Supplier not found"
                        )
                );

        SupplierContract contract =
                new SupplierContract();

        contract.setSupplier(supplier);

        contract.setContractName(
                request.getContractName()
        );

        contract.setDescription(
                request.getDescription()
        );

        contract.setStartDate(
                request.getStartDate()
        );

        contract.setEndDate(
                request.getEndDate()
        );

        contract.setStatus(
                calculateStatus(
                        request.getStartDate(),
                        request.getEndDate()
                )
        );

        contract.setCreatedDate(
                LocalDateTime.now()
        );

        SupplierContract saved =
                contractRepository.save(contract);

        return convertToResponse(saved);
    }

    // GET ALL
    @Override
    public List<SupplierContractResponse>
    getAllContracts() {

        updateStatuses();

        return contractRepository
                .findAll()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // GET BY ID
    @Override
    public SupplierContractResponse getContractById(
            Long id
    ) {

        SupplierContract contract =
                contractRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Contract not found"
                                )
                        );

        updateStatus(contract);

        return convertToResponse(contract);
    }

    // GET BY SUPPLIER
    @Override
    public List<SupplierContractResponse>
    getContractsBySupplier(
            Long supplierId
    ) {

        return contractRepository
                .findBySupplier_SupplierId(supplierId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // GET BY STATUS
    @Override
    public List<SupplierContractResponse>
    getContractsByStatus(
            String status
    ) {

        return contractRepository
                .findByStatus(status.toUpperCase())
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // SEARCH
    @Override
    public List<SupplierContractResponse>
    searchContracts(
            String name
    ) {

        return contractRepository
                .findByContractNameContainingIgnoreCase(name)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // UPDATE
    @Override
    public SupplierContractResponse updateContract(
            Long id,
            SupplierContractRequest request
    ) {

        validateRequest(request);

        SupplierContract contract =
                contractRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Contract not found"
                                )
                        );

        Supplier supplier =
                supplierRepository
                        .findById(
                                request.getSupplierId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Supplier not found"
                                )
                        );

        contract.setSupplier(supplier);

        contract.setContractName(
                request.getContractName()
        );

        contract.setDescription(
                request.getDescription()
        );

        contract.setStartDate(
                request.getStartDate()
        );

        contract.setEndDate(
                request.getEndDate()
        );

        contract.setStatus(
                calculateStatus(
                        request.getStartDate(),
                        request.getEndDate()
                )
        );

        SupplierContract updated =
                contractRepository.save(contract);

        return convertToResponse(updated);
    }

    // DELETE
    @Override
    public String deleteContract(Long id) {

        if (!contractRepository.existsById(id)) {

            throw new RuntimeException(
                    "Contract not found"
            );
        }

        contractRepository.deleteById(id);

        return "Contract deleted successfully";
    }

    // VALIDATION
    private void validateRequest(
            SupplierContractRequest request
    ) {

        if (request.getSupplierId() == null) {

            throw new RuntimeException(
                    "Supplier ID is required"
            );
        }

        if (request.getContractName() == null ||
                request.getContractName()
                        .trim().isEmpty()) {

            throw new RuntimeException(
                    "Contract name is required"
            );
        }

        if (request.getStartDate() == null) {

            throw new RuntimeException(
                    "Start date is required"
            );
        }

        if (request.getEndDate() == null) {

            throw new RuntimeException(
                    "End date is required"
            );
        }

        if (request.getEndDate()
                .isBefore(request.getStartDate())) {

            throw new RuntimeException(
                    "End date cannot be before start date"
            );
        }
    }

    // STATUS
    private String calculateStatus(
            LocalDate startDate,
            LocalDate endDate
    ) {

        LocalDate today = LocalDate.now();

        if (today.isBefore(startDate)) {
            return "UPCOMING";
        }

        if (today.isAfter(endDate)) {
            return "EXPIRED";
        }

        return "ACTIVE";
    }

    private void updateStatus(
            SupplierContract contract
    ) {

        String status =
                calculateStatus(
                        contract.getStartDate(),
                        contract.getEndDate()
                );

        if (!status.equals(
                contract.getStatus())) {

            contract.setStatus(status);

            contractRepository.save(contract);
        }
    }

    private void updateStatuses() {

        List<SupplierContract> contracts =
                contractRepository.findAll();

        for (SupplierContract contract : contracts) {
            updateStatus(contract);
        }
    }

    // CONVERT
    private SupplierContractResponse
    convertToResponse(
            SupplierContract contract
    ) {

        Supplier supplier =
                contract.getSupplier();

        return new SupplierContractResponse(

                contract.getContractId(),

                supplier.getSupplierId(),

                supplier.getSupplierName(),

                contract.getContractName(),

                contract.getDescription(),

                contract.getStartDate(),

                contract.getEndDate(),

                contract.getStatus(),

                contract.getCreatedDate()
        );
    }
}
