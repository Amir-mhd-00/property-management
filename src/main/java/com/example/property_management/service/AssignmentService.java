package com.example.property_management.service;

import com.example.property_management.dto.AssignmentDTO;
import com.example.property_management.dto.CreateAssignmentRequestDTO;
import com.example.property_management.entity.AssignmentEntity;

import java.util.List;

public interface AssignmentService {

    AssignmentDTO createAssignment(CreateAssignmentRequestDTO DTO);
    AssignmentDTO findById(Long id);
    List<AssignmentDTO> findAll();
    AssignmentDTO end(Long id);
}
