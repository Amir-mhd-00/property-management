package com.example.property_management.controller;

import com.example.property_management.dto.PropertyDTO;
import com.example.property_management.dto.PropertyUpdateDTO;
import com.example.property_management.error.ErrorResponse;
import com.example.property_management.service.impl.PropertyServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

@Tag(
        name = "Property management",
        description = "operations for managing properties"
)
@RestController
@RequestMapping("/api/v1")
public class PropertyController {

    private final PropertyServiceImpl propertyService;

    public PropertyController(PropertyServiceImpl propertyService) {
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
            content = @Content(schema = @Schema(implementation = PropertyDTO.class))
    )
    @ApiResponse(responseCode = "404", description = "Property not found",
            content =  @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/properties/{id}")
    public ResponseEntity<PropertyDTO> getProperty(
            @Parameter(description = "Unique identifier of the property", example = "1")
            @PathVariable Long id) {

        logger.info("GET request for property with id {}", id);

        PropertyDTO property = propertyService.getProperty(id);

        return ResponseEntity.ok(property);
    }

    @Operation(
            summary = "Create a property",
            description = "Creates a new property and returns the created resource."
    )
    @ApiResponse(
            responseCode = "201",
            description = "Property created successfully",
            content = @Content(schema = @Schema(implementation = PropertyDTO.class))
    )
    @ApiResponse(responseCode = "400", description = "Invalid request body")
    @ApiResponse(responseCode = "409", description = "property already exists")
    @PostMapping("/properties")
    public ResponseEntity<PropertyDTO> createProperty(
            @Valid @RequestBody PropertyDTO propertyDTO) {

        logger.info("POST request for creating property {}", propertyDTO.getPropertyName());

        PropertyDTO savedProperty = propertyService.createProperty(propertyDTO);

        return new ResponseEntity<>(savedProperty, HttpStatus.CREATED); // new response -> return Response
    }

    @Operation(summary = "Getting all properties", description = "Returns a list of all registered properties.")
    @ApiResponse(responseCode = "200",
            description = "properties fetched successfully",
            content = @Content(
                    mediaType = "application/json",
                    array = @io.swagger.v3.oas.annotations.media.ArraySchema(
                            schema = @Schema(implementation = PropertyDTO.class)))
    )
    @ApiResponse(responseCode = "500", description = "internal server error")
    @GetMapping("/properties")
    public ResponseEntity<List<PropertyDTO>> getAllProperties(){

        logger.info("GET request for getting all properties");

        return ResponseEntity.ok(propertyService.getAllProperties());
    }

    @Operation(summary = "Updating a property", description = "Replaces all updatable fields of an existing property.")
    @ApiResponse(responseCode = "200",
            description = "Property updated successfully",
            content = @Content(schema = @Schema(implementation = PropertyDTO.class))
    )
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @ApiResponse(responseCode = "404", description = "Property not found",
            content =  @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request body")
    @PutMapping("/properties/{id}")
    public ResponseEntity<PropertyDTO> updateProperty(
            @Parameter(description = "Unique identifier of the property", example = "1")
            @PathVariable Long id,
            @RequestBody PropertyUpdateDTO propertyDTO){

        logger.info("PUT request for updating property with id {}", id);

        PropertyDTO updatedProperty = propertyService.updateProperty(id, propertyDTO);

        return ResponseEntity.ok(updatedProperty);
    }

    @Operation(summary = "Partially update a property",
            description = "Updates only the fields supplied in the request body.")
    @ApiResponse(
            responseCode = "200",
            description = "Property updated successfully",
            content = @Content(schema = @Schema(implementation = PropertyDTO.class))
    )
    @ApiResponse(responseCode = "404", description = "Property not found",
            content =  @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request body")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @PatchMapping("/properties/{id}")
    public ResponseEntity<PropertyDTO> partialUpdateProperty(
            @Parameter(description = "Unique identifier of the property", example = "1")
            @PathVariable Long id, @RequestBody PropertyUpdateDTO propertyUpdateDTO){

        logger.info("PATCH request for updating property with id {}", id);

        PropertyDTO updatedProperty = propertyService.partialUpdateProperty(id, propertyUpdateDTO);

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
    @DeleteMapping("/properties/{id}")//deleteProperty or deleteproperty
    public ResponseEntity<Void>  deleteProperty(@PathVariable Long id){

        logger.info("DELETE request for deleting property with id {}", id);

        propertyService.deleteProperty(id);

        return ResponseEntity.noContent().build();
    }
}