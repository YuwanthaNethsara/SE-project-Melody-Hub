package com.example.project1.pattern.adapter;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Finds the adapter for a format name. Spring injects every SupplierFeedAdapter bean. */
@Component
public class SupplierFeedAdapterRegistry {

    private final Map<String, SupplierFeedAdapter> adapters = new TreeMap<>();

    public SupplierFeedAdapterRegistry(List<SupplierFeedAdapter> feedAdapters) {
        for (SupplierFeedAdapter adapter : feedAdapters) {
            adapters.put(adapter.getFormat().toUpperCase(), adapter);
        }
    }

    public SupplierFeedAdapter forFormat(String format) {
        SupplierFeedAdapter adapter = format == null ? null : adapters.get(format.trim().toUpperCase());
        if (adapter == null) {
            throw new IllegalArgumentException(
                    "Unsupported supplier data format. Supported formats: " + String.join(", ", adapters.keySet()));
        }
        return adapter;
    }

    public Map<String, String> describeAll() {
        Map<String, String> description = new TreeMap<>();
        adapters.forEach((name, adapter) -> description.put(name, adapter.describe()));
        return description;
    }
}
