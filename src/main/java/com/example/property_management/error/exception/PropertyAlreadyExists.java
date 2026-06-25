package com.example.property_management.error.exception;

public class PropertyAlreadyExists extends RuntimeException {
    public PropertyAlreadyExists(String message) {
        super(message);
    }
}
