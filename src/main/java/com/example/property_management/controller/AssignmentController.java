package com.example.property_management.controller;

import com.example.property_management.dto.PageResponse;
import com.example.property_management.dto.assignment.AssignmentDTO;
import com.example.property_management.dto.assignment.CreateAssignmentRequestDTO;
import com.example.property_management.service.AssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Assignment management",
        description = "operations for managing assignments"
)
@RestController
@RequestMapping("/api/v1/assignments")
public class AssignmentController {

    private static final Logger log = LoggerFactory.getLogger(AssignmentController.class);

    private  final AssignmentService assignmentService;
    public AssignmentController(AssignmentService assignmentService) {

        this.assignmentService = assignmentService;
    }

    @Operation(
            summary = "Create a new assignment",
            description = "Creates a new assignment that links a property to a user."
    )
    @ApiResponse(
            responseCode = "201",
            description = "Assignment created successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AssignmentDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Invalid request body"
    )
    @ApiResponse(
            responseCode = "404",
            description = "Property or user not found"
    )
    @ApiResponse(
            responseCode = "409",
            description = "Property is already assigned"
    )
    @PostMapping
    public ResponseEntity<AssignmentDTO> createAssignment(
            @Valid @RequestBody CreateAssignmentRequestDTO dto) {

        log.info("POST request for creating a new  assignment");

        AssignmentDTO assignment = assignmentService.createAssignment(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(assignment);
    }

    @Operation(
            summary = "Get assignment by ID",
            description = "Retrieves a single assignment using its unique identifier."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Assignment retrieved successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AssignmentDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Assignment not found"
    )
    @GetMapping("/{id}")
    public ResponseEntity<AssignmentDTO> getAssignment(@PathVariable Long id) {

        log.info("GET request for getting an assignment with id: {}", id);

        AssignmentDTO assignment = assignmentService.findById(id);

        return ResponseEntity.ok(assignment);
    }

    @Operation(
            summary = "Get all assignments",
            description = "Retrieves a list of all assignments in the system."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Assignments retrieved successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AssignmentDTO.class)
            )
    )
    @GetMapping
    public ResponseEntity<PageResponse<AssignmentDTO>> getAllAssignments(@ParameterObject Pageable pageable) {

        log.info("GET request for getting all assignments");

        return ResponseEntity.ok(assignmentService.findAll(pageable));
    }

    @Operation(
            summary = "End an assignment",
            description = "Marks an active assignment as ended."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Assignment ended successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AssignmentDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Assignment not found"
    )
    @ApiResponse(
            responseCode = "409",
            description = "Assignment has already ended"
    )
    @PatchMapping("/{id}/end")
    public ResponseEntity<AssignmentDTO> endAssignment(@PathVariable Long id) {

        log.info("PATCH request for ending the assignment id={}",id);

        AssignmentDTO response = assignmentService.end(id);

        return ResponseEntity.ok(response);
    }
}
