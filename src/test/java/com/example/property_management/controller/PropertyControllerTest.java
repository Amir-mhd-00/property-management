package com.example.property_management.controller;

import com.example.property_management.dto.PageResponse;
import com.example.property_management.dto.assignment.AssignmentDTO;
import com.example.property_management.dto.property.PropertyCreateDTO;
import com.example.property_management.dto.property.PropertyPatchDTO;
import com.example.property_management.dto.property.PropertyResponseDTO;
import com.example.property_management.dto.property.PropertyUpdateDTO;
import com.example.property_management.enums.PropertyStatus;
import com.example.property_management.enums.PropertyType;
import com.example.property_management.error.exception.PropertyAlreadyExistsException;
import com.example.property_management.error.exception.PropertyNotFoundException;
import com.example.property_management.service.PropertyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PropertyController.class)
@AutoConfigureMockMvc(addFilters = false)
class PropertyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PropertyService propertyService;

    private PropertyCreateDTO validCreateDTO() {
        PropertyCreateDTO dto = new PropertyCreateDTO();
        dto.setPropertyName("Sunset Villa");
        dto.setPropertyValue(100000.0);
        dto.setPropertyType(PropertyType.VILLA);
        dto.setPropertyStatus(PropertyStatus.AVAILABLE);
        dto.setLocation("123 Main St");
        dto.setOwnerId(1L);
        return dto;
    }

    @Test
    void getProperty_returnsProperty() throws Exception {
        PropertyResponseDTO dto = new PropertyResponseDTO();
        dto.setId(1L);
        dto.setPropertyName("Sunset Villa");

        when(propertyService.getProperty(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/properties/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.propertyName").value("Sunset Villa"));
    }

    @Test
    void getProperty_notFound_returns404() throws Exception {
        when(propertyService.getProperty(99L)).thenThrow(new PropertyNotFoundException("not found"));

        mockMvc.perform(get("/api/v1/properties/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    void createProperty_valid_returns201() throws Exception {
        PropertyCreateDTO dto = validCreateDTO();

        PropertyResponseDTO responseDTO = new PropertyResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setPropertyName("Sunset Villa");

        when(propertyService.createProperty(any(PropertyCreateDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/v1/properties")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void createProperty_missingRequiredFields_returns400() throws Exception {
        PropertyCreateDTO dto = new PropertyCreateDTO();

        mockMvc.perform(post("/api/v1/properties")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createProperty_alreadyExists_returns409() throws Exception {
        PropertyCreateDTO dto = validCreateDTO();

        when(propertyService.createProperty(any(PropertyCreateDTO.class)))
                .thenThrow(new PropertyAlreadyExistsException("already exists"));

        mockMvc.perform(post("/api/v1/properties")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    void getProperties_returnsPagedResults() throws Exception {
        PropertyResponseDTO dto = new PropertyResponseDTO();
        dto.setId(1L);

        PageResponse<PropertyResponseDTO> response =
                new PageResponse<>(List.of(dto), 0, 20, 1, 1, true, true);

        when(propertyService.getProperties(any())).thenReturn(response);

        mockMvc.perform(get("/api/v1/properties"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1));
    }

    @Test
    void updateProperty_valid_returns200() throws Exception {
        PropertyUpdateDTO dto = new PropertyUpdateDTO(
                "villa", 200000.0, PropertyType.HOUSE, PropertyStatus.AVAILABLE, 4, "456 test Blv");

        PropertyResponseDTO responseDTO = new PropertyResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setPropertyName("villa");

        when(propertyService.updateProperty(eq(1L), any(PropertyUpdateDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(put("/api/v1/properties/{id}", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.propertyName").value("villa"));
    }

    @Test
    void updateProperty_invalidBody_returns400() throws Exception {
        PropertyUpdateDTO dto = new PropertyUpdateDTO(
                "", null, null, null, null, "");

        mockMvc.perform(put("/api/v1/properties/{id}", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void partialUpdateProperty_returns200() throws Exception {
        PropertyPatchDTO dto = new PropertyPatchDTO(
                "newName", null, null, null, null, null);

        PropertyResponseDTO responseDTO = new PropertyResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setPropertyName("newName");

        when(propertyService.partialUpdateProperty(eq(1L), any(PropertyPatchDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(patch("/api/v1/properties/{id}", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.propertyName").value("newName"));
    }

    @Test
    void deleteProperty_success_returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/properties/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(propertyService).deleteProperty(1L);
    }

    @Test
    void findAllByPropertyStatus_returnsFilteredList() throws Exception {
        PropertyResponseDTO dto = new PropertyResponseDTO();
        dto.setId(1L);
        dto.setPropertyStatus(PropertyStatus.AVAILABLE);

        when(propertyService.getAllPropertiesByStatus(PropertyStatus.AVAILABLE)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/properties/status").param("status", "AVAILABLE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void findAllByPropertyStatus_invalidEnum_returnsError() throws Exception {
        // An invalid enum value for a @RequestParam is not caught by any specific
        // @ExceptionHandler in GlobalExceptionHandler, so it falls through to the
        // generic Exception handler (500). This test documents current behavior.
        mockMvc.perform(get("/api/v1/properties/status").param("status", "NOT_A_STATUS"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getAllAssignments_returnsAssignmentsForProperty() throws Exception {
        AssignmentDTO assignment = new AssignmentDTO();
        assignment.setId(1L);
        assignment.setPropertyId(1L);

        when(propertyService.getAssignmentsByProperty(1L)).thenReturn(List.of(assignment));

        mockMvc.perform(get("/api/v1/properties/{propertyId}/assignments", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].propertyId").value(1));
    }
}
