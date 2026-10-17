package com.example.project1.dto;

import java.util.List;

public class SupplierImportResponse {

    private String format;
    private int total;
    private int imported;
    private List<SupplierResponse> suppliers;
    private List<String> skipped;

    public SupplierImportResponse() {
    }

    public SupplierImportResponse(String format, int total, int imported,
                                  List<SupplierResponse> suppliers, List<String> skipped) {
        this.format = format;
        this.total = total;
        this.imported = imported;
        this.suppliers = suppliers;
        this.skipped = skipped;
    }

    public String getFormat() { return format; }
    public int getTotal() { return total; }
    public int getImported() { return imported; }
    public List<SupplierResponse> getSuppliers() { return suppliers; }
    public List<String> getSkipped() { return skipped; }
}
