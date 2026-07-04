package com.example.property_management.service;

import com.example.property_management.dto.LoginRequestDTO;
import com.example.property_management.dto.LoginResponseDTO;

public interface AuthenticationService {
    LoginResponseDTO login(LoginRequestDTO request);
    void logout();
}
