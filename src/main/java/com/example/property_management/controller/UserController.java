package com.example.property_management.controller;

import com.example.property_management.dto.*;
import com.example.property_management.error.ErrorResponse;
import com.example.property_management.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "User Management",
        description = "Operations for managing users"
)
@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account."
    )
    @ApiResponse(responseCode = "201",
            description = "User registered successfully",
            content = @Content(
                    schema = @Schema(implementation = UserResponseDTO.class)
            )
    )
    @ApiResponse(responseCode = "409", description = "user already exists")
    @ApiResponse(responseCode = "400", description = "validation error")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @ApiResponse(responseCode = "400", description = "invalid fields")
    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> createUser(
            @Valid @RequestBody RegisterUserDTO dto) {

        logger.info("POST request for creating user {}", dto.getEmail());

        UserResponseDTO createdUser = userService.register(dto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(createdUser);
    }


    @Operation(
            summary = "Authenticate user",
        description = "Authenticates a user using their email and password."
    )
    @ApiResponse(responseCode = "200",
            description = "user authenticated successfully"
            // content = JWT Token
    )
    @ApiResponse(responseCode = "404", description = "user not found",
        content =  @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @ApiResponse(responseCode = "400", description = "invalid fields")
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO dto) {

        logger.info("Login attempt for email {}", dto.getEmail());

        LoginResponseDTO user = userService.login(dto);

        return ResponseEntity.ok(user);
    }
}
