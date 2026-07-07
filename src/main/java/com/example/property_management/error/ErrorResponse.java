package com.example.property_management.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private Map<String, String> errors;

    public  ErrorResponse(int status, String error, String message) {
        this.timestamp = LocalDateTime.now(ZoneId.of("UTC"));
        this.status = status;
        this.error = error;
        this.message = message;
        this.errors = null;
    }

}
