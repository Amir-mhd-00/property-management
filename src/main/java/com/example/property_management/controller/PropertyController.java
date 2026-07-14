package com.example.property_management.controller;

import com.example.property_management.dto.assignment.AssignmentDTO;
import com.example.property_management.dto.PageResponse;
import com.example.property_management.dto.property.PropertyCreateDTO;
import com.example.property_management.dto.property.PropertyResponseDTO;
import com.example.property_management.dto.property.PropertyUpdateDTO;
import com.example.property_management.error.ErrorResponse;
import com.example.property_management.service.PropertyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import java.util.List;

@Tag(
        name = "Property management",
        description = "operations for managing properties"
)
@RestController
@RequestMapping("/api/v1/properties")
public class PropertyController {

    private final PropertyService propertyService;

    public PropertyController(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    private static final Logger logger = LoggerFactory.getLogger(PropertyController.class);

    @Operation(
            summary = "Get a property by ID",
            description = "Retrieves the details of a property using its unique identifier."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Property retrieved successfully",
            content = @Content(schema = @Schema(implementation = PropertyResponseDTO.class))
    )
    @ApiResponse(responseCode = "404", description = "Property not found",
            content =  @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/{id}")
    public ResponseEntity<PropertyResponseDTO> getProperty(
            @Parameter(description = "Unique identifier of the property", example = "1")
            @PathVariable Long id) {

        logger.info("GET request for property with id {}", id);

        PropertyResponseDTO property = propertyService.getProperty(id);

        return ResponseEntity.ok(property);
    }

    @Operation(
            summary = "Create a property",
            description = "Creates a new property and returns the created resource."
    )
    @ApiResponse(
            responseCode = "201",
            description = "Property created successfully",
            content = @Content(schema = @Schema(implementation = PropertyResponseDTO.class))
    )
    @ApiResponse(responseCode = "400", description = "Invalid request body")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "409", description = "property already exists")
    @PostMapping
    public ResponseEntity<PropertyResponseDTO> createProperty(
            @Valid @RequestBody PropertyCreateDTO property) {

        logger.info("POST request for creating property {}", property.getPropertyName());

        PropertyResponseDTO savedProperty = propertyService.createProperty(property);

        return new ResponseEntity<>(savedProperty, HttpStatus.CREATED); // new response -> return Response
    }

    @Operation(summary = "Getting all properties", description = "Returns a list of all registered properties.")
    @ApiResponse(responseCode = "200",
            description = "properties fetched successfully",
            content = @Content(
                    mediaType = "application/json",
                    array = @io.swagger.v3.oas.annotations.media.ArraySchema(
                            schema = @Schema(implementation = PropertyResponseDTO.class)))
    )
    @ApiResponse(responseCode = "500", description = "internal server error")
    @GetMapping
    public ResponseEntity<PageResponse<PropertyResponseDTO>> getProperties(@ParameterObject Pageable pageable) {

        logger.info("Get request for fetching all users");

        return ResponseEntity.ok(propertyService.getProperties(pageable));
    }

    @Operation(summary = "Updating a property", description = "Replaces all updatable fields of an existing property.")
    @ApiResponse(responseCode = "200",
            description = "Property updated successfully",
            content = @Content(schema = @Schema(implementation = PropertyResponseDTO.class))
    )
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @ApiResponse(responseCode = "404", description = "Property not found",
            content =  @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request body")
    @ApiResponse(responseCode = "404", description = "user not found",
            content =  @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PutMapping("/{id}")
    public ResponseEntity<PropertyResponseDTO> updateProperty(
            @Parameter(description = "Unique identifier of the property", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody PropertyCreateDTO propertyCreateDTO){

        logger.info("PUT request for updating property with id {}", id);

        PropertyResponseDTO updatedProperty = propertyService.updateProperty(id, propertyCreateDTO);

        return ResponseEntity.ok(updatedProperty);
    }

    @Operation(summary = "Partially update a property",
            description = "Updates only the fields supplied in the request body.")
    @ApiResponse(
            responseCode = "200",
            description = "Property updated successfully",
            content = @Content(schema = @Schema(implementation = PropertyResponseDTO.class))
    )
    @ApiResponse(responseCode = "409", description = "Property already exists",
            content =  @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request body")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @PatchMapping("/{id}")
    public ResponseEntity<PropertyResponseDTO> partialUpdateProperty(
            @Parameter(description = "Unique identifier of the property", example = "1")
            @PathVariable Long id, @RequestBody PropertyUpdateDTO propertyUpdateDTO){

        logger.info("PATCH request for updating property with id {}", id);

        PropertyResponseDTO updatedProperty = propertyService.partialUpdateProperty(id, propertyUpdateDTO);

        return ResponseEntity.ok(updatedProperty);
    }

    @Operation(
            summary = "Delete a property",
            description = "Deletes a property identified by its unique identifier."
    )
    @ApiResponse(responseCode = "204", description = "Property deleted successfully")
    @ApiResponse(responseCode = "404", description = "Property not found",
            content =  @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void>  deleteProperty(@PathVariable Long id){

        logger.info("DELETE request for deleting property with id {}", id);

        propertyService.deleteProperty(id);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Getting properties by status", description = "Returns a list of all registered properties grouped by status.")
    @ApiResponse(responseCode = "200",
            description = "properties fetched successfully",
            content = @Content(
                    mediaType = "application/json",
                    array = @io.swagger.v3.oas.annotations.media.ArraySchema(
                            schema = @Schema(implementation = PropertyResponseDTO.class)))
    )
    @ApiResponse(responseCode = "500", description = "internal server error")
    @GetMapping("/status")
    public ResponseEntity<List<PropertyResponseDTO>> findAllByPropertyStatus(@RequestParam String status){

        logger.info("GET request for finding all properties with status {}", status);

        return ResponseEntity.ok(propertyService.getAllPropertiesByStatus(status));
    }


    @Operation(
            summary = "Get all assignments for a property",
            description = "Returns all assignments associated with the specified property."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Assignments retrieved successfully"
    )
    @ApiResponse(
            responseCode = "404",
            description = "Property not found",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @GetMapping("/{propertyId}/assignments")
    public ResponseEntity<List<AssignmentDTO>> getAllAssignments(
            @PathVariable Long propertyId) {

        logger.info("GET request for fetching all assignments for property with id {}", propertyId);

        List<AssignmentDTO> response = propertyService.getAssignmentsByProperty(propertyId);

        return ResponseEntity.ok(response);
    }
}