package com.example.property_management.service;

import com.example.property_management.dto.assignment.AssignmentDTO;
import com.example.property_management.dto.PageResponse;
import com.example.property_management.dto.property.PropertyCreateDTO;
import com.example.property_management.dto.property.PropertyResponseDTO;
import com.example.property_management.dto.property.PropertyUpdateDTO;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface PropertyService {
    PropertyResponseDTO createProperty(PropertyCreateDTO property);
    PropertyResponseDTO getProperty(Long id);
    PageResponse<PropertyResponseDTO> getProperties(Pageable pageable);
    PropertyResponseDTO updateProperty(Long id, PropertyCreateDTO dto);
    PropertyResponseDTO partialUpdateProperty(Long id, PropertyUpdateDTO dto);
    void deleteProperty(Long id);
    List<PropertyResponseDTO> getAllPropertiesByStatus(String propertyStatus);
    List<AssignmentDTO> getAssignmentsByProperty(Long id);

}
