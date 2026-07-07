package com.example.property_management.service;

import com.example.property_management.dto.LoginRequestDTO;
import com.example.property_management.dto.LoginResponseDTO;
import com.example.property_management.dto.UserRegisterDTO;
import com.example.property_management.dto.UserResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthenticationService {
    UserResponseDTO register(UserRegisterDTO dto);
    LoginResponseDTO login(LoginRequestDTO request, HttpServletRequest httpServletRequest,
                           HttpServletResponse httpServletResponse);
}
