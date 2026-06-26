package com.example.property_management.controller;

import com.example.property_management.dto.*;
import com.example.property_management.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> createUser(
            @Valid @RequestBody RegisterUserDTO dto) {

        logger.info("POST request for creating user {}", dto.getEmail());

        UserResponseDTO createdUser = userService.register(dto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(createdUser);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO dto) {

        logger.info("POST request for login ");

        LoginResponseDTO user = userService.login(dto);

        return ResponseEntity.ok(user);
    }
}
