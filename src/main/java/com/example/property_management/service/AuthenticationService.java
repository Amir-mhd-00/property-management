package com.example.property_management.service;

import com.example.property_management.dto.auth.LoginRequestDTO;
import com.example.property_management.dto.auth.LoginResponseDTO;
import com.example.property_management.dto.user.UserRegisterDTO;
import com.example.property_management.dto.user.UserResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthenticationService {
    UserResponseDTO register(UserRegisterDTO dto);
    LoginResponseDTO login(LoginRequestDTO request, HttpServletRequest httpServletRequest,
                           HttpServletResponse httpServletResponse);
}
