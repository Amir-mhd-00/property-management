package com.example.property_management.controller;

import com.example.property_management.dto.assignment.AssignmentDTO;
import com.example.property_management.dto.user.UserResponseDTO;
import com.example.property_management.dto.user.UserUpdateDTO;
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
@RequestMapping("/api/v1/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(
            summary = "Get user by ID",
            description = "Retrieves a single user by their unique ID."
    )
    @ApiResponse(
            responseCode = "200",
            description = "User retrieved successfully"
    )
    @ApiResponse(
            responseCode = "403",
            description = "Insufficient permissions"
    )
    @ApiResponse(
            responseCode = "404",
            description = "User not found"
    )
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable("id") Long userId) {

        logger.info("GET request for fetching user with id {}", userId);

        UserResponseDTO userResponseDTO = userService.getUserById(userId);

        return ResponseEntity.ok(userResponseDTO);
    }

    @Operation(
            summary = "Get all users",
            description = "Retrieves a list of all users that the authenticated user is authorized to view."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Users retrieved successfully"
    )
    @ApiResponse(
            responseCode = "403",
            description = "Insufficient permissions"
    )
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {

        logger.info("GET request for fetching all users");

        List<UserResponseDTO> userResponseDTOs = userService.getAllUsers();

        return ResponseEntity.ok(userResponseDTOs);
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
    @ApiResponse(
            responseCode = "403",
            description = "Insufficient permissions"
    )
    @GetMapping("/{userId}/assignments")
    public ResponseEntity<List<AssignmentDTO>> getAllAssignmentsByUser(
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
    @ApiResponse(
            responseCode = "403",
            description = "Insufficient permissions"
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
    @ApiResponse(
            responseCode = "403",
            description = "Insufficient permissions"
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") Long userId) {

        logger.info("DELETE request for deleting user with id {}", userId);

        userService.deleteUser(userId);

        return ResponseEntity.noContent().build();
    }

}

