package com.example.property_management.service;

import com.example.property_management.dto.LoginRequestDTO;
import com.example.property_management.dto.LoginResponseDTO;
import com.example.property_management.dto.RegisterUserDTO;
import com.example.property_management.dto.UserResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthenticationService {
    UserResponseDTO register(RegisterUserDTO dto);
    LoginResponseDTO login(LoginRequestDTO request, HttpServletRequest httpServletRequest,
                           HttpServletResponse httpServletResponse);
}
