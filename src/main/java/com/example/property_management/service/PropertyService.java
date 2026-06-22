package com.example.property_management.service;

import com.example.property_management.DTO.PropertyDTO;
import com.example.property_management.DTO.PropertyUpdateDTO;

import java.util.List;

public interface PropertyService {
    PropertyDTO createProperty(PropertyDTO propertyDTO);
    List<PropertyDTO> getAllProperties();
    PropertyDTO updateProperty(Long id,  PropertyDTO propertyDTO);
    PropertyDTO updateProperty(Long id, PropertyUpdateDTO dto);

}
