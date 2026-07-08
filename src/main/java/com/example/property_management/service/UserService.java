package com.example.property_management.service;

import com.example.property_management.dto.*;

import java.util.List;

public interface UserService {
    UserResponseDTO getUserById(long id);
    List<UserResponseDTO> getAllUsers();
    List<AssignmentDTO> getAssignmentsByUser(Long id);
    UserResponseDTO updateUser(Long id, UserUpdateDTO userUpdateDTO);
    void deleteUser(Long id);
}