package com.example.property_management.service;

import com.example.property_management.dto.AssignmentDTO;
import com.example.property_management.dto.CreateAssignmentRequestDTO;

public interface AssignmentService {

    AssignmentDTO createAssignment(CreateAssignmentRequestDTO DTO);
}
