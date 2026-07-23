package com.example.property_management.controller;

import com.example.property_management.dto.auth.LoginRequestDTO;
import com.example.property_management.dto.auth.LoginResponseDTO;
import com.example.property_management.dto.user.UserRegisterDTO;
import com.example.property_management.dto.user.UserResponseDTO;
import com.example.property_management.entity.UserEntity;
import com.example.property_management.enums.UserRole;
import com.example.property_management.error.exception.InvalidCredentialsException;
import com.example.property_management.error.exception.UserAlreadyExistsException;
import com.example.property_management.security.CustomUserDetails;
import com.example.property_management.service.AuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthenticationController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthenticationService authenticationService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ---------- register ----------

    @Test
    void register_valid_returns201() throws Exception {
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setEmail("john@example.com");
        dto.setPassword("password123");

        UserResponseDTO response = new UserResponseDTO();
        response.setId(1L);
        response.setEmail("john@example.com");

        when(authenticationService.register(any(UserRegisterDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void register_invalidEmail_returns400() throws Exception {
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setEmail("not-an-email");
        dto.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_missingRequiredFields_returns400() throws Exception {
        UserRegisterDTO dto = new UserRegisterDTO();

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_emailAlreadyExists_returns409() throws Exception {
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setEmail("john@example.com");
        dto.setPassword("password123");

        when(authenticationService.register(any(UserRegisterDTO.class)))
                .thenThrow(new UserAlreadyExistsException("Email Already Exists"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    // ---------- login ----------

    @Test
    void login_validCredentials_returns200() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("john@example.com");
        request.setPassword("password123");

        LoginResponseDTO response = LoginResponseDTO.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .role("GUEST")
                .build();

        when(authenticationService.login(any(), any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void login_invalidBody_returns400() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("");
        request.setPassword("");

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_badCredentials_returns401() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("john@example.com");
        request.setPassword("wrongPassword");

        when(authenticationService.login(any(), any(), any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_invalidCredentialsException_returns401() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("john@example.com");
        request.setPassword("wrongPassword");

        when(authenticationService.login(any(), any(), any()))
                .thenThrow(new InvalidCredentialsException("invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // ---------- logout ----------

    @Test
    void logout_authenticatedUser_returns200() throws Exception {
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setRole(UserRole.GUEST);
        user.setEmail("john@example.com");

        CustomUserDetails details = new CustomUserDetails(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities()));

        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk());
    }

    // ---------- me ----------

    @Test
    void me_authenticatedUser_returnsUserInfo() throws Exception {
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john@example.com");
        user.setRole(UserRole.AGENT);

        CustomUserDetails details = new CustomUserDetails(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities()));

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.role").value("AGENT"));
    }

    @Test
    void me_unauthenticated_returns401() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}
