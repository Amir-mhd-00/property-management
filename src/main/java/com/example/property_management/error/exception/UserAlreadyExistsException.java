package com.example.property_management.error.exception;

public class UserAlreadyExistsException extends RuntimeException  {

    public UserAlreadyExistsException(String message) {
        super(message);
    }

}
