package com.example.project1.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Short factory methods for the errors services throw, so every module answers with the
 * right HTTP status and a message a normal user understands.
 */
public final class ApiErrors {

    private ApiErrors() {
    }

    public static ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }

    public static ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }

    public static ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    public static ResponseStatusException forbidden(String message) {
        return new ResponseStatusException(HttpStatus.FORBIDDEN, message);
    }

    /** Throws 400 with the given message when the value is null. */
    public static <T> T required(T value, String message) {
        if (value == null) {
            throw badRequest(message);
        }
        return value;
    }

    /** Trims and checks a required text value (blank -> 400, too long -> 400). */
    public static String requiredText(String value, String label, int maxLength) {
        String clean = value == null ? "" : value.trim();
        if (clean.isEmpty()) {
            throw badRequest(label + " is required.");
        }
        if (clean.length() > maxLength) {
            throw badRequest(label + " must be at most " + maxLength + " characters.");
        }
        return clean;
    }

    /** Trims an optional text value; blank becomes null. */
    public static String optionalText(String value, String label, int maxLength) {
        String clean = value == null ? "" : value.trim();
        if (clean.isEmpty()) {
            return null;
        }
        if (clean.length() > maxLength) {
            throw badRequest(label + " must be at most " + maxLength + " characters.");
        }
        return clean;
    }
}
