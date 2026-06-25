package com.example.property_management.controller;

import com.example.property_management.dto.*;
import com.example.property_management.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> createUser(
            @Valid @RequestBody RegisterUserDTO dto) {

        UserResponseDTO createdUser = userService.register(dto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(createdUser);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO dto) {

        LoginResponseDTO user = userService.login(dto);

        return ResponseEntity.ok(user);
    }
}
