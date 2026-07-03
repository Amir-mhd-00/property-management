package com.example.property_management.error.exception;

public class PropertyAlreadyAssignedException extends RuntimeException {
    public PropertyAlreadyAssignedException(String message) {
        super(message);
    }
}
