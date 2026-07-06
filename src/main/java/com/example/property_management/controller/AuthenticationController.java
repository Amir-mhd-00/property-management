package com.example.property_management.controller;

import com.example.property_management.dto.LoginRequestDTO;
import com.example.property_management.dto.LoginResponseDTO;
import com.example.property_management.service.AuthenticationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

        private final AuthenticationService
                authenticationService;

        public AuthenticationController(AuthenticationService authenticationService) {

            this.authenticationService =
                    authenticationService;
        }

        @PostMapping("/login")
        public ResponseEntity<LoginResponseDTO> login(
                @RequestBody LoginRequestDTO request) {

            return ResponseEntity.ok(authenticationService.login(request));

    }
}
//Logout
//Refresh token (when you add JWT)
//Forgot password
//Reset password
//Verify email