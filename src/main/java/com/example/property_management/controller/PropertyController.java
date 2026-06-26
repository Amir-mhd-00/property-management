package com.example.property_management.controller;

import com.example.property_management.dto.PropertyDTO;
import com.example.property_management.dto.PropertyUpdateDTO;
import com.example.property_management.service.impl.PropertyServiceImpl;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class PropertyController {

    private final PropertyServiceImpl propertyService;

    public PropertyController(PropertyServiceImpl propertyService) {
        this.propertyService = propertyService;
    }

    private static final Logger logger = LoggerFactory.getLogger(PropertyController.class);

    @GetMapping("/properties/{id}")
    public ResponseEntity<PropertyDTO> property(@PathVariable Long id){

        logger.info("GET request for property with id {}", id);

        PropertyDTO property = propertyService.getProperty(id);

        return ResponseEntity.ok(property);
    }

    @PostMapping("/properties")
    public ResponseEntity<PropertyDTO> property(@Valid @RequestBody PropertyDTO propertyDTO){

        logger.info("POST request for creating property {}", propertyDTO.getPropertyName());

        PropertyDTO savedProperty = propertyService.createProperty(propertyDTO);

        return new ResponseEntity<>(savedProperty, HttpStatus.CREATED); // new response -> return Response
    }

    @GetMapping("/properties")
    public ResponseEntity<List<PropertyDTO>> getAllProperties(){

        logger.info("GET request for getting all properties");

        return ResponseEntity.ok(propertyService.getAllProperties());
    }

    @PutMapping("/properties/{id}")
    public ResponseEntity<PropertyDTO> updateProperty(@PathVariable Long id, @RequestBody PropertyDTO propertyDTO){

        logger.info("PUT request for updating property with id {}", id);

        PropertyDTO updatedProperty = propertyService.updateProperty(id, propertyDTO);

        return ResponseEntity.ok(updatedProperty);
    }

    @PatchMapping("/properties/{id}")
    public ResponseEntity<PropertyDTO> partialUpdateProperty(@PathVariable Long id, @RequestBody PropertyUpdateDTO propertyUpdateDTO){

        logger.info("PATCH request for updating property with id {}", id);

        PropertyDTO updatedProperty = propertyService.partialUpdateProperty(id, propertyUpdateDTO);

        return ResponseEntity.ok(updatedProperty);
    }
    
    @DeleteMapping("/properties/{id}")//deleteProperty or deleteproperty
    public ResponseEntity<Void>  deleteProperty(@PathVariable Long id){

        logger.info("DELETE request for deleting property with id {}", id);

        propertyService.deleteProperty(id);

        return ResponseEntity.noContent().build();
    }
}