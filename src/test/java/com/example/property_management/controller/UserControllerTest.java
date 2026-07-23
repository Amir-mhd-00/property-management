package com.example.property_management.controller;

import com.example.property_management.dto.PageResponse;
import com.example.property_management.dto.assignment.AssignmentDTO;
import com.example.property_management.dto.user.UserResponseDTO;
import com.example.property_management.dto.user.UserUpdateDTO;
import com.example.property_management.enums.UserRole;
import com.example.property_management.error.exception.ForbiddenException;
import com.example.property_management.error.exception.UserNotFoundException;
import com.example.property_management.service.UserService;
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

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @Test
    void getUserById_returnsUser() throws Exception {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(1L);
        dto.setFirstName("John");
        dto.setRole(UserRole.GUEST);

        when(userService.getUserById(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/users/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    void getUserById_notFound_returns404() throws Exception {
        when(userService.getUserById(99L)).thenThrow(new UserNotFoundException("user not found"));

        mockMvc.perform(get("/api/v1/users/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    void getUserById_forbidden_returns403() throws Exception {
        when(userService.getUserById(1L)).thenThrow(new ForbiddenException("nope"));

        mockMvc.perform(get("/api/v1/users/{id}", 1L))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllUsers_returnsPagedUsers() throws Exception {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(1L);

        PageResponse<UserResponseDTO> response =
                new PageResponse<>(List.of(dto), 0, 20, 1, 1, true, true);

        when(userService.getAllUsers(any())).thenReturn(response);

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getAllAssignmentsByUser_returnsAssignments() throws Exception {
        AssignmentDTO assignment = new AssignmentDTO();
        assignment.setId(1L);
        assignment.setUserId(5L);

        when(userService.getAssignmentsByUser(5L)).thenReturn(List.of(assignment));

        mockMvc.perform(get("/api/v1/users/{userId}/assignments", 5L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].userId").value(5));
    }

    @Test
    void updateUser_validRequest_returnsUpdatedUser() throws Exception {
        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setFirstName("Jane");
        updateDTO.setLastName("Doe");
        updateDTO.setEmail("jane@example.com");

        UserResponseDTO response = new UserResponseDTO();
        response.setId(1L);
        response.setFirstName("Jane");

        when(userService.updateUser(eq(1L), any(UserUpdateDTO.class))).thenReturn(response);

        mockMvc.perform(patch("/api/v1/users/{id}", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jane"));
    }

    @Test
    void updateUser_invalidEmail_returns400() throws Exception {
        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setEmail("not-an-email");

        mockMvc.perform(patch("/api/v1/users/{id}", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteUser_success_returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/users/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(1L);
    }

    @Test
    void deleteUser_notFound_returns404() throws Exception {
        org.mockito.Mockito.doThrow(new UserNotFoundException("not found"))
                .when(userService).deleteUser(99L);

        mockMvc.perform(delete("/api/v1/users/{id}", 99L))
                .andExpect(status().isNotFound());
    }
}
