package com.example.property_management.controller;

import com.example.property_management.dto.*;
import com.example.property_management.error.ErrorResponse;
import com.example.property_management.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "User Management",
        description = "Operations for managing users"
)
@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }



    @Operation(
            summary = "Get all assignments for a user",
            description = "Returns all assignments associated with the specified user."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Assignments retrieved successfully"
    )
    @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @GetMapping("/{userId}/assignments")
    public ResponseEntity<List<AssignmentDTO>> getAllAssignments(
            @Parameter(
                    description = "ID of the user whose assignments should be retrieved",
                    required = true,
                    example = "1"
            )
            @PathVariable Long userId) {

        logger.info("GET request for fetching all assignments for user with id {}", userId);

        List<AssignmentDTO> response = userService.findByUser(userId);

        return ResponseEntity.ok(response);
    }
}

//Update a user
//Delete a user
//Get user information