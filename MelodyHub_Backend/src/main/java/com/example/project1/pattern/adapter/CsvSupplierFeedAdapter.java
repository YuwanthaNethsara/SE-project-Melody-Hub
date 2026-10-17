package com.example.project1.pattern.adapter;

import com.example.project1.dto.SupplierRequest;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapter for a spreadsheet / ERP export. The first line names the columns, using the supplier's
 * own words:  company, contact, email, phone, address, status  (any order; address and status optional).
 */
@Component
public class CsvSupplierFeedAdapter implements SupplierFeedAdapter {

    @Override
    public String getFormat() {
        return "CSV";
    }

    @Override
    public String describe() {
        return "Header line + one supplier per line: company,contact,email,phone,address,status";
    }

    @Override
    public List<SupplierRequest> toSupplierRequests(String rawData) {
        List<String> lines = new ArrayList<>();
        for (String line : rawData.split("\\R")) {
            if (!line.isBlank()) {
                lines.add(line);
            }
        }
        if (lines.size() < 2) {
            throw new IllegalArgumentException("CSV needs a header line and at least one supplier line");
        }

        Map<String, Integer> column = new HashMap<>();
        List<String> header = splitLine(lines.get(0));
        for (int i = 0; i < header.size(); i++) {
            column.put(header.get(i).trim().toLowerCase(), i);
        }
        for (String required : List.of("company", "contact", "email", "phone")) {
            if (!column.containsKey(required)) {
                throw new IllegalArgumentException("CSV header must contain the column '" + required + "'");
            }
        }

        List<SupplierRequest> result = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            List<String> cells = splitLine(lines.get(i));
            SupplierRequest request = new SupplierRequest();
            request.setSupplierName(cell(cells, column, "company"));
            request.setContactPerson(cell(cells, column, "contact"));
            request.setEmail(cell(cells, column, "email"));
            request.setPhone(cell(cells, column, "phone"));
            request.setAddress(cell(cells, column, "address"));
            request.setStatus(cell(cells, column, "status"));
            result.add(request);
        }
        return result;
    }

    private static String cell(List<String> cells, Map<String, Integer> column, String name) {
        Integer index = column.get(name);
        if (index == null || index >= cells.size()) {
            return null;
        }
        String value = cells.get(index).trim();
        return value.isEmpty() ? null : value;
    }

    /** Splits one CSV line; a value in double quotes may contain commas. */
    private static List<String> splitLine(String line) {
        List<String> cells = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    quoted = !quoted;
                }
            } else if (c == ',' && !quoted) {
                cells.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        cells.add(current.toString());
        return cells;
    }
}
