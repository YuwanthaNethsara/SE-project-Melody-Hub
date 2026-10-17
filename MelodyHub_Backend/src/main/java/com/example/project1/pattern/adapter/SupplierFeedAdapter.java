package com.example.project1.pattern.adapter;

import com.example.project1.dto.SupplierRequest;

import java.util.List;

/**
 * ADAPTER pattern (Structural) - Supplier Management.
 *
 * Target interface: what SupplierServiceImpl understands (a list of SupplierRequest).
 * Suppliers and their systems send their contact lists in their own formats. Each adapter wraps one
 * foreign format and converts it into the target, so the core supplier logic (validation,
 * duplicate e-mail check, saving) is reused unchanged for every format.
 *
 * No external supplier API exists in MelodyHub, so these adapters work on data a user uploads
 * (an exported file); a new supplier system = one new adapter class.
 */
public interface SupplierFeedAdapter {

    /** Format name used by the API, e.g. CSV or XML. */
    String getFormat();

    /** Human readable description of the layout the adapter expects. */
    String describe();

    /** Converts raw supplier data into the system's own request objects. */
    List<SupplierRequest> toSupplierRequests(String rawData);
}
