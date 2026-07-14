package com.example.property_management.service;

import com.example.property_management.dto.PageResponse;
import com.example.property_management.dto.assignment.AssignmentDTO;
import com.example.property_management.dto.assignment.CreateAssignmentRequestDTO;
import org.springframework.data.domain.Pageable;

public interface AssignmentService {

    AssignmentDTO createAssignment(CreateAssignmentRequestDTO DTO);
    AssignmentDTO findById(Long id);
    PageResponse<AssignmentDTO> findAll(Pageable pageable);
    AssignmentDTO end(Long id);
}
