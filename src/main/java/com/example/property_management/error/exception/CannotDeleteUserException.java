package com.example.property_management.error.exception;

public class CannotDeleteUserException extends RuntimeException {
    public CannotDeleteUserException(String message) {
        super(message);
    }
}
