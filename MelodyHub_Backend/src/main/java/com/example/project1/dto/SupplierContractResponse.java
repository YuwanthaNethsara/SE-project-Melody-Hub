package com.example.project1.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class SupplierContractResponse {

    private Long contractId;

    private Long supplierId;
    private String supplierName;

    private String contractName;
    private String description;

    private LocalDate startDate;
    private LocalDate endDate;

    private String status;

    private LocalDateTime createdDate;

    public SupplierContractResponse() {
    }

    public SupplierContractResponse(
            Long contractId,
            Long supplierId,
            String supplierName,
            String contractName,
            String description,
            LocalDate startDate,
            LocalDate endDate,
            String status,
            LocalDateTime createdDate
    ) {
        this.contractId = contractId;
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.contractName = contractName;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.createdDate = createdDate;
    }

    public Long getContractId() {
        return contractId;
    }

    public void setContractId(Long contractId) {
        this.contractId = contractId;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getContractName() {
        return contractName;
    }

    public void setContractName(String contractName) {
        this.contractName = contractName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
}
