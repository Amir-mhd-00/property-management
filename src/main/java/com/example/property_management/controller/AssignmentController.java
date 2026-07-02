package com.example.property_management.controller;

import com.example.property_management.dto.AssignmentDTO;
import com.example.property_management.dto.CreateAssignmentRequestDTO;
import com.example.property_management.service.impl.AssignmentServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "Assignment management",
        description = "operations for managing assignments"
)
@RestController
@RequestMapping("/api/v1/assignments")
public class AssignmentController {

    private static final Logger logger = LoggerFactory.getLogger(AssignmentController.class);

    private  final AssignmentServiceImpl assignmentService;
    public AssignmentController(AssignmentServiceImpl assignmentService) {
        this.assignmentService = assignmentService;
    }

    @Operation(
            summary = "Create an assignment",
            description = "Creates a new assignment for a property and user."
    )
    @ApiResponse(
            responseCode = "201",
            description = "Property created successfully",
            content = @Content(schema = @Schema(implementation = CreateAssignmentRequestDTO.class))
    )
    @ApiResponse(responseCode = "400", description = "Invalid request body")
    @ApiResponse(responseCode = "409", description = "property already exists")
    @PostMapping
    public ResponseEntity<AssignmentDTO> createAssignment(@Valid @RequestBody CreateAssignmentRequestDTO dto) {

        logger.info("POST request for creating a new  assignment");

        AssignmentDTO assignment = assignmentService.createAssignment(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(assignment);
    }
}
