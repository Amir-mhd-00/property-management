package com.example.property_management.service;

import com.example.property_management.dto.*;

public interface UserService {

    UserResponseDTO register(RegisterUserDTO userDTO);
    LoginResponseDTO login(LoginRequestDTO loginRequestDTO);

}
