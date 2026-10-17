package com.example.project1.controller;

import com.example.project1.dto.SupplierContractRequest;
import com.example.project1.dto.SupplierContractResponse;
import com.example.project1.service.SupplierContractService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/supplier-contracts")
public class SupplierContractController {

    private final SupplierContractService contractService;

    public SupplierContractController(
            SupplierContractService contractService
    ) {
        this.contractService = contractService;
    }

    // CREATE
    @PostMapping
    public ResponseEntity<SupplierContractResponse>
    createContract(
            @RequestBody SupplierContractRequest request
    ) {

        return new ResponseEntity<>(
                contractService.createContract(request),
                HttpStatus.CREATED
        );
    }

    // GET ALL
    @GetMapping
    public ResponseEntity<List<SupplierContractResponse>>
    getAllContracts() {

        return ResponseEntity.ok(
                contractService.getAllContracts()
        );
    }

    // GET BY SUPPLIER
    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<List<SupplierContractResponse>>
    getContractsBySupplier(
            @PathVariable Long supplierId
    ) {

        return ResponseEntity.ok(
                contractService
                        .getContractsBySupplier(supplierId)
        );
    }

    // GET BY STATUS
    @GetMapping("/status/{status}")
    public ResponseEntity<List<SupplierContractResponse>>
    getContractsByStatus(
            @PathVariable String status
    ) {

        return ResponseEntity.ok(
                contractService
                        .getContractsByStatus(status)
        );
    }

    // SEARCH
    @GetMapping("/search")
    public ResponseEntity<List<SupplierContractResponse>>
    searchContracts(
            @RequestParam String name
    ) {

        return ResponseEntity.ok(
                contractService.searchContracts(name)
        );
    }

    // GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<SupplierContractResponse>
    getContractById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                contractService.getContractById(id)
        );
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<SupplierContractResponse>
    updateContract(
            @PathVariable Long id,
            @RequestBody SupplierContractRequest request
    ) {

        return ResponseEntity.ok(
                contractService.updateContract(
                        id,
                        request
                )
        );
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<String>
    deleteContract(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                contractService.deleteContract(id)
        );
    }
}
