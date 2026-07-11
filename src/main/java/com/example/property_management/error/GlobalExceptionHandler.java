package com.example.property_management.error;

import com.example.property_management.error.exception.*;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException ex) {

        logger.warn("User not found {}", ex.getMessage());

        return BuildErrorResponse(
                HttpStatus.NOT_FOUND,
                "NOT_FOUND",
                ex.getMessage()
        );
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public  ResponseEntity<ErrorResponse> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {

        logger.warn("User already exists {}", ex.getMessage());

        return BuildErrorResponse(
                HttpStatus.CONFLICT,
                "CONFLICT",
                ex.getMessage()
        );
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentialsException(InvalidCredentialsException ex) {

        logger.warn("Invalid credentials: {}", ex.getMessage());

        return BuildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                "UNAUTHORIZED",
                ex.getMessage()
        );
    }

    @ExceptionHandler(PropertyNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePropertyNotFoundException(PropertyNotFoundException ex) {

        logger.warn("Property not found: {}", ex.getMessage());

        return BuildErrorResponse(
                HttpStatus.NOT_FOUND,
                "NOT_FOUND",
                ex.getMessage()
        );
    }

    @ExceptionHandler(PropertyAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handlePropertyAlreadyExistsException(PropertyAlreadyExistsException ex) {

        logger.warn("Property already exists {}", ex.getMessage());

        return BuildErrorResponse(
                HttpStatus.CONFLICT,
                "CONFLICT",
                ex.getMessage()
        );
    }

    @ExceptionHandler(AssignmentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAssignmentNotFoundException(AssignmentNotFoundException ex) {

        logger.warn("Assignment not found: {}", ex.getMessage());

        return BuildErrorResponse(
                HttpStatus.NOT_FOUND,
                "NOT_FOUND",
                ex.getMessage()
        );
    }

    @ExceptionHandler(PropertyAlreadyAssignedException.class)
    public ResponseEntity<ErrorResponse> handlePropertyAlreadyAssigned(PropertyAlreadyAssignedException ex){

        logger.warn("Property already assigned: {}", ex.getMessage());

        return BuildErrorResponse(
                HttpStatus.CONFLICT,
                "Conflict",
                ex.getMessage()
        );
    }

    @ExceptionHandler(AssignmentAlreadyInactiveException.class)
    public ResponseEntity<ErrorResponse> handleAssignmentAlreadyInactiveException(AssignmentAlreadyInactiveException ex) {

        logger.warn("Assignment already inactive: {}", ex.getMessage());

        return BuildErrorResponse(
                HttpStatus.CONFLICT,
                "CONFLICT",
                ex.getMessage()
        );
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(UnauthorizedException ex) {

        logger.warn("Unauthorized: {}", ex.getMessage());

        return BuildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                "UNAUTHORIZED",
                ex.getMessage()
        );
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbiddenException(ForbiddenException ex) {

        logger.warn("Forbidden: {}", ex.getMessage());

        return BuildErrorResponse(
          HttpStatus.FORBIDDEN,
          "FORBIDDEN",
          ex.getMessage()
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {

        logger.warn("Http message not readable: {}", ex.getMessage());

        Throwable cause = ex.getCause();

        if (cause instanceof InvalidFormatException invalidFormatException) {

            Class<?> targetType = invalidFormatException.getTargetType();

            if (targetType.isEnum()) {

                String values = Arrays.stream(targetType.getEnumConstants())
                        .map(Object::toString)
                        .collect(Collectors.joining(", "));

                return BuildErrorResponse(
                        HttpStatus.BAD_REQUEST,
                        "INVALID_FORMAT",
                        ("Invalid value. Allowed values: " + values));
            }
        }

        return BuildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid json request",
                ex.getMessage()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.put(
                                error.getField(),
                                error.getDefaultMessage()
                        ));

        logger.warn("Validation failed : {}", errors);

        ErrorResponse errorResponse = new ErrorResponse(
            LocalDateTime.now(ZoneId.of("UTC")),
            HttpStatus.BAD_REQUEST.value(),
            "BAD REQUEST",
            null,
            errors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex, HttpServletRequest request) {

        logger.error(
                "Unhandled exception. method={}, url={}, exception={}",
                request.getMethod(),
                request.getRequestURI(),
                ex.getClass().getSimpleName(),
                ex
        );

        return BuildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL SERVER ERROR",
                ex.getMessage()
        );
    }


    private ResponseEntity<ErrorResponse> BuildErrorResponse(
            HttpStatus status,
            String error,
            String message) {

        ErrorResponse errorResponse = new ErrorResponse(
                status.value(),
                error,
                message
        );

        return ResponseEntity.status(status).body(errorResponse);
    }
}
