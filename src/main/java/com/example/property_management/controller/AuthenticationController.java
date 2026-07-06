package com.example.property_management.controller;

import com.example.property_management.dto.LoginRequestDTO;
import com.example.property_management.dto.LoginResponseDTO;
import com.example.property_management.dto.RegisterUserDTO;
import com.example.property_management.dto.UserResponseDTO;
import com.example.property_management.security.CustomUserDetails;
import com.example.property_management.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);

    public AuthenticationController(AuthenticationService authenticationService) {

        this.authenticationService =
                authenticationService;
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

        UserResponseDTO createdUser = authenticationService.register(dto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(createdUser);
    }

    @Operation(
            summary = "Login user",
            description = "Authenticates a user using email and password. " +
                    "If the credentials are valid, a session is created and " +
                    "the authenticated user's information is returned."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Login successful"
    )
    @ApiResponse(
            responseCode = "401",
            description = "Invalid email or password"
    )
    @ApiResponse(
            responseCode = "400",
            description = "Invalid request body"
    )
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @Valid @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Email and password"
            )
            LoginRequestDTO request,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) {

        return ResponseEntity.ok(authenticationService.login(
                request, httpServletRequest, httpServletResponse));
    }


    @Operation(
            summary = "Get current authenticated user",
            description = "Returns the profile information of the currently authenticated user."
    )

    @ApiResponse(
            responseCode = "200",
            description = "Authenticated user information returned successfully"
    )
    @ApiResponse(
            responseCode = "401",
            description = "User is not authenticated"
    )

    @GetMapping("/me")
    public LoginResponseDTO me(
            @AuthenticationPrincipal CustomUserDetails user) {

        return new LoginResponseDTO(
                user.getUser().getId(),
                user.getUser().getFirstName(),
                user.getUser().getLastName(),
                user.getUsername(),
                user.getUser().getRole().name()
        );
    }

}
//Forgot password
//Reset password
