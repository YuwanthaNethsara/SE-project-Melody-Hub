package com.example.project1.controller;

import com.example.project1.dto.SupplierImportRequest;
import com.example.project1.dto.SupplierImportResponse;
import com.example.project1.dto.SupplierRequest;
import com.example.project1.dto.SupplierResponse;
import com.example.project1.service.SupplierService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(
            SupplierService supplierService
    ) {
        this.supplierService = supplierService;
    }

    // ==========================================
    // IMPORT FROM ANOTHER SYSTEM'S FORMAT (Adapter pattern)
    // ==========================================

    @PostMapping("/import")
    public ResponseEntity<SupplierImportResponse>
    importSuppliers(
            @RequestBody SupplierImportRequest request
    ) {

        return ResponseEntity.ok(
                supplierService.importSuppliers(request)
        );
    }

    @GetMapping("/import/formats")
    public ResponseEntity<Map<String, String>>
    getImportFormats() {

        return ResponseEntity.ok(
                supplierService.getSupportedImportFormats()
        );
    }

    // ==========================================
    // CREATE
    // ==========================================

    @PostMapping
    public ResponseEntity<SupplierResponse>
    createSupplier(
            @RequestBody SupplierRequest request
    ) {

        return new ResponseEntity<>(
                supplierService.createSupplier(request),
                HttpStatus.CREATED
        );
    }

    // ==========================================
    // GET ALL
    // ==========================================

    @GetMapping
    public ResponseEntity<List<SupplierResponse>>
    getAllSuppliers() {

        return ResponseEntity.ok(
                supplierService.getAllSuppliers()
        );
    }

    // ==========================================
    // GET BY STATUS
    // ==========================================

    @GetMapping("/status/{status}")
    public ResponseEntity<List<SupplierResponse>>
    getSuppliersByStatus(
            @PathVariable String status
    ) {

        return ResponseEntity.ok(
                supplierService
                        .getSuppliersByStatus(status)
        );
    }

    // ==========================================
    // SEARCH
    // ==========================================

    @GetMapping("/search")
    public ResponseEntity<List<SupplierResponse>>
    searchSuppliers(
            @RequestParam String name
    ) {

        return ResponseEntity.ok(
                supplierService.searchSuppliers(name)
        );
    }

    // ==========================================
    // GET BY ID
    // ==========================================

    @GetMapping("/{id}")
    public ResponseEntity<SupplierResponse>
    getSupplierById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                supplierService
                        .getSupplierById(id)
        );
    }

    // ==========================================
    // UPDATE
    // ==========================================

    @PutMapping("/{id}")
    public ResponseEntity<SupplierResponse>
    updateSupplier(
            @PathVariable Long id,
            @RequestBody SupplierRequest request
    ) {

        return ResponseEntity.ok(
                supplierService.updateSupplier(
                        id,
                        request
                )
        );
    }

    // ==========================================
    // DELETE
    // ==========================================

    @DeleteMapping("/{id}")
    public ResponseEntity<String>
    deleteSupplier(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                supplierService.deleteSupplier(id)
        );
    }
}
