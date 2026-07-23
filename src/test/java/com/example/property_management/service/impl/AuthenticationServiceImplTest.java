package com.example.property_management.service.impl;

import com.example.property_management.dto.auth.LoginRequestDTO;
import com.example.property_management.dto.auth.LoginResponseDTO;
import com.example.property_management.dto.user.UserRegisterDTO;
import com.example.property_management.dto.user.UserResponseDTO;
import com.example.property_management.entity.UserEntity;
import com.example.property_management.enums.UserRole;
import com.example.property_management.error.exception.UserAlreadyExistsException;
import com.example.property_management.mapper.UserMapper;
import com.example.property_management.repository.UserRepository;
import com.example.property_management.security.CustomUserDetails;
import com.example.property_management.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private SecurityContextRepository securityContextRepository;
    @Mock private AuditLogService auditLogService;
    @Mock private UserMapper userMapper;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    private UserEntity userEntity;

    @BeforeEach
    void setUp() {
        userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setFirstName("John");
        userEntity.setLastName("Doe");
        userEntity.setEmail("john@example.com");
        userEntity.setRole(UserRole.GUEST);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ---------- register ----------

    @Test
    void register_success() {
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setEmail("john@example.com");
        dto.setPassword("plainPassword");

        UserEntity mappedEntity = new UserEntity();

        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(userMapper.toEntity(dto)).thenReturn(mappedEntity);
        when(passwordEncoder.encode("plainPassword")).thenReturn("encodedPassword");
        when(userRepository.save(mappedEntity)).thenReturn(userEntity);

        UserResponseDTO responseDTO = new UserResponseDTO();
        responseDTO.setId(1L);
        when(userMapper.toDTO(userEntity)).thenReturn(responseDTO);

        UserResponseDTO result = authenticationService.register(dto);

        assertEquals(1L, result.getId());
        assertEquals("encodedPassword", mappedEntity.getPassword());
        assertEquals(UserRole.GUEST, mappedEntity.getRole());
        verify(auditLogService).userLog(eq("User"), eq("1"), eq("Create"), any(), any());
    }

    @Test
    void register_emailAlreadyExists_throws() {
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setEmail("john@example.com");

        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> authenticationService.register(dto));
        verify(userRepository, never()).save(any());
    }

    // ---------- login ----------

    @Test
    void login_success() {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("john@example.com");
        request.setPassword("plainPassword");

        HttpServletRequest httpRequest = mock(HttpServletRequest.class);
        HttpServletResponse httpResponse = mock(HttpServletResponse.class);

        CustomUserDetails userDetails = new CustomUserDetails(userEntity);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        doNothing().when(securityContextRepository).saveContext(any(), eq(httpRequest), eq(httpResponse));

        LoginResponseDTO result = authenticationService.login(request, httpRequest, httpResponse);

        assertEquals(1L, result.getId());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("john@example.com", result.getEmail());
        assertEquals("GUEST", result.getRole());

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        verify(securityContextRepository).saveContext(any(), eq(httpRequest), eq(httpResponse));
    }

    @Test
    void login_authenticationFails_propagatesException() {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("john@example.com");
        request.setPassword("wrongPassword");

        HttpServletRequest httpRequest = mock(HttpServletRequest.class);
        HttpServletResponse httpResponse = mock(HttpServletResponse.class);

        when(authenticationManager.authenticate(any()))
                .thenThrow(new org.springframework.security.authentication.BadCredentialsException("bad credentials"));

        assertThrows(org.springframework.security.authentication.BadCredentialsException.class,
                () -> authenticationService.login(request, httpRequest, httpResponse));

        verify(securityContextRepository, never()).saveContext(any(), any(), any());
    }
}
