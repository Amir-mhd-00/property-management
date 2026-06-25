package com.example.property_management.error.exception;

public class PropertyAlreadyExistsException extends RuntimeException {
    public PropertyAlreadyExistsException(String message) {
        super(message);
    }
}
