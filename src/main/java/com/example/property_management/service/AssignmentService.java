package com.example.property_management.service;

import com.example.property_management.dto.assignment.AssignmentDTO;
import com.example.property_management.dto.assignment.CreateAssignmentRequestDTO;

import java.util.List;

public interface AssignmentService {

    AssignmentDTO createAssignment(CreateAssignmentRequestDTO DTO);
    AssignmentDTO findById(Long id);
    List<AssignmentDTO> findAll();
    AssignmentDTO end(Long id);
}
