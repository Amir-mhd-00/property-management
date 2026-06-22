package com.example.property_management.controller;

import com.example.property_management.DTO.PropertyDTO;
import com.example.property_management.service.PropertyServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class PropertyController {

    private final PropertyServiceImpl propertyService;

    public PropertyController(PropertyServiceImpl propertyService) {
        this.propertyService = propertyService;
    }

    @GetMapping("/Hello")
    public String helloWorld(){
        return "World";
    }

    @PostMapping("/property")
    public ResponseEntity<PropertyDTO> property(@RequestBody PropertyDTO propertyDTO){

        PropertyDTO savedProperty = propertyService.createProperty(propertyDTO);
        return new ResponseEntity<>(savedProperty, HttpStatus.CREATED);
    }

    @GetMapping("/properties")
    public ResponseEntity<List<PropertyDTO>> getAllProperties(){
        return ResponseEntity.ok(propertyService.getAllProperties());
    }

    @PutMapping("/updateproperty/{id}")
    public ResponseEntity<PropertyDTO> updateProperty(@PathVariable Long id, @RequestBody PropertyDTO propertyDTO){
        PropertyDTO updatedProperty = propertyService.updateProperty(id, propertyDTO);

        return new ResponseEntity<>(updatedProperty, HttpStatus.OK);
    }
}