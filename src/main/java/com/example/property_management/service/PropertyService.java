package com.example.property_management.service;

import com.example.property_management.dto.AssignmentDTO;
import com.example.property_management.dto.PropertyDTO;
import com.example.property_management.dto.PropertyUpdateDTO;
import java.util.List;

public interface PropertyService {
    PropertyDTO createProperty(PropertyDTO propertyDTO);
    PropertyDTO getProperty(Long id);
    List<PropertyDTO> getAllProperties();
    PropertyDTO updateProperty(Long id, PropertyUpdateDTO dto);
    PropertyDTO partialUpdateProperty(Long id, PropertyUpdateDTO dto);
    void deleteProperty(Long id);
    List<PropertyDTO> getAllPropertiesByStatus(String propertyStatus);
    List<AssignmentDTO> getAssignmentsByProperty(Long id);

}
