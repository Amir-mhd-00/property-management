package com.example.property_management.service;

import com.example.property_management.dto.PageResponse;
import com.example.property_management.dto.assignment.AssignmentDTO;
import com.example.property_management.dto.user.UserResponseDTO;
import com.example.property_management.dto.user.UserUpdateDTO;
import org.springframework.data.domain.Pageable;


import java.util.List;

public interface UserService {
    UserResponseDTO getUserById(long id);
    PageResponse<UserResponseDTO> getAllUsers(Pageable pageable);
    List<AssignmentDTO> getAssignmentsByUser(Long id);
    UserResponseDTO updateUser(Long id, UserUpdateDTO userUpdateDTO);
    void deleteUser(Long id);
}