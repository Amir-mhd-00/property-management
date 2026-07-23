package com.example.property_management.controller;

import com.example.property_management.dto.PageResponse;
import com.example.property_management.dto.assignment.AssignmentDTO;
import com.example.property_management.dto.assignment.CreateAssignmentRequestDTO;
import com.example.property_management.enums.AssignmentRole;
import com.example.property_management.enums.AssignmentStatus;
import com.example.property_management.error.exception.AssignmentAlreadyInactiveException;
import com.example.property_management.error.exception.AssignmentNotFoundException;
import com.example.property_management.error.exception.PropertyAlreadyAssignedException;
import com.example.property_management.service.AssignmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AssignmentController.class)
@AutoConfigureMockMvc(addFilters = false)
class AssignmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AssignmentService assignmentService;

    private CreateAssignmentRequestDTO validRequest() {
        return new CreateAssignmentRequestDTO(
                1L, 2L, AssignmentRole.PROPERTY_MANAGER,
                LocalDate.now(), LocalDate.now().plusMonths(1),
                AssignmentStatus.ACTIVE, false);
    }

    @Test
    void createAssignment_valid_returns201() throws Exception {
        AssignmentDTO responseDTO = new AssignmentDTO();
        responseDTO.setId(1L);
        responseDTO.setPropertyId(1L);
        responseDTO.setUserId(2L);

        when(assignmentService.createAssignment(any(CreateAssignmentRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/v1/assignments")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.propertyId").value(1))
                .andExpect(jsonPath("$.userId").value(2));
    }

    @Test
    void createAssignment_missingFields_returns400() throws Exception {
        CreateAssignmentRequestDTO dto = new CreateAssignmentRequestDTO();

        mockMvc.perform(post("/api/v1/assignments")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAssignment_propertyAlreadyAssigned_returns409() throws Exception {
        when(assignmentService.createAssignment(any(CreateAssignmentRequestDTO.class)))
                .thenThrow(new PropertyAlreadyAssignedException("already assigned"));

        mockMvc.perform(post("/api/v1/assignments")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isConflict());
    }

    @Test
    void getAssignment_returnsAssignment() throws Exception {
        AssignmentDTO dto = new AssignmentDTO();
        dto.setId(1L);

        when(assignmentService.findById(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/assignments/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getAssignment_notFound_returns404() throws Exception {
        when(assignmentService.findById(99L)).thenThrow(new AssignmentNotFoundException("not found"));

        mockMvc.perform(get("/api/v1/assignments/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllAssignments_returnsPagedResults() throws Exception {
        AssignmentDTO dto = new AssignmentDTO();
        dto.setId(1L);

        PageResponse<AssignmentDTO> response =
                new PageResponse<>(List.of(dto), 0, 20, 1, 1, true, true);

        when(assignmentService.findAll(any())).thenReturn(response);

        mockMvc.perform(get("/api/v1/assignments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1));
    }

    @Test
    void endAssignment_success_returns200() throws Exception {
        AssignmentDTO dto = new AssignmentDTO();
        dto.setId(1L);
        dto.setStatus(AssignmentStatus.INACTIVE);

        when(assignmentService.end(1L)).thenReturn(dto);

        mockMvc.perform(patch("/api/v1/assignments/{id}/end", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    void endAssignment_alreadyInactive_returns409() throws Exception {
        when(assignmentService.end(1L)).thenThrow(new AssignmentAlreadyInactiveException("already inactive"));

        mockMvc.perform(patch("/api/v1/assignments/{id}/end", 1L))
                .andExpect(status().isConflict());
    }
}
