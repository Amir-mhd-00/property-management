package com.example.property_management.service;

import com.example.property_management.dto.PropertyDTO;
import com.example.property_management.dto.PropertyUpdateDTO;

import java.util.List;

public interface PropertyService {
    PropertyDTO createProperty(PropertyDTO propertyDTO);
    PropertyDTO getProperty(Long id);
    List<PropertyDTO> getAllProperties();
    PropertyDTO updateProperty(Long id, PropertyDTO propertyDTO);
    PropertyDTO partialUpdateProperty(Long id, PropertyUpdateDTO dto);
    void deleteProperty(Long id);

}
