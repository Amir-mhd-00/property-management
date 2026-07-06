package com.example.property_management.controller;

import com.example.property_management.dto.LoginRequestDTO;
import com.example.property_management.dto.LoginResponseDTO;
import com.example.property_management.security.CustomUserDetails;
import com.example.property_management.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
                @RequestBody LoginRequestDTO request,
                HttpServletRequest httpServletRequest,
                HttpServletResponse httpServletResponse) {

            return ResponseEntity.ok(authenticationService.login(
                    request, httpServletRequest, httpServletResponse));
        }

        @GetMapping("/me")
        public LoginResponseDTO me(@AuthenticationPrincipal CustomUserDetails user) {

            return new LoginResponseDTO(
                    user.getUser().getId(),
                    user.getUser().getFirstName(),
                    user.getUser().getLastName(),
                    user.getUsername(),
                    user.getUser().getRole().name()
            );
        }

}
//Logout
//Refresh token (when you add JWT)
//Forgot password
//Reset password
//Verify email