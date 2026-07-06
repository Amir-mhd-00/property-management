package com.example.property_management.service;

import com.example.property_management.dto.LoginRequestDTO;
import com.example.property_management.dto.LoginResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthenticationService {
    LoginResponseDTO login(LoginRequestDTO request, HttpServletRequest httpServletRequest,
                           HttpServletResponse httpServletResponse);
    void logout();
}
