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
import jakarta.validation.Valid;
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

        List<AssignmentDTO> response = userService.getAssignmentsByUser(userId);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Partially update a user",
            description = "Updates one or more fields of an existing user. Only the fields provided in the request body are modified."
    )
    @ApiResponse(
            responseCode = "200",
            description = "User updated successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserUpdateDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Validation failed or invalid request",
            content = @Content
    )
    @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content
    )
    @ApiResponse(
            responseCode = "409",
            description = "Email already exists",
            content = @Content
    )
    @PatchMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(

            @Parameter(
                    description = "ID of the user to update",
                    required = true,
                    example = "1"
            )
            @PathVariable("id") Long userId,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Fields to update",
                    required = true
            )
            @Valid @RequestBody UserUpdateDTO userUpdateDTO) {

        logger.info("PATCH request for patching user with id {}", userId);

        UserResponseDTO response = userService.updateUser(userId, userUpdateDTO);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Delete a user",
            description = "Deletes a user identified by its unique identifier."
    )
    @ApiResponse(responseCode = "204", description = "user deleted successfully")
    @ApiResponse(responseCode = "404", description = "user not found",
            content =  @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") Long userId) {

        logger.info("DELETE request for deleting user with id {}", userId);

        userService.deleteUser(userId);

        return ResponseEntity.noContent().build();
    }

}

//Get user information