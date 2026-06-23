package com.example.property_management.service;

import com.example.property_management.dto.LoginRequestDTO;
import com.example.property_management.dto.UserDTO;

public interface UserService {

    UserDTO register(UserDTO userDTO);
    UserDTO login(LoginRequestDTO loginRequestDTO);

}
