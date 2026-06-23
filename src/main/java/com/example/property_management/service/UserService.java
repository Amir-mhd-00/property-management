package com.example.property_management.service;

import com.example.property_management.DTO.UserDTO;

public interface UserService {

    UserDTO register(UserDTO userDTO);
    UserDTO login(String password, String ownerEmail);

}
