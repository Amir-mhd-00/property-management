package com.example.property_management.service.impl;

import com.example.property_management.authorization.UserAuthorizationService;
import com.example.property_management.dto.PageResponse;
import com.example.property_management.dto.assignment.AssignmentDTO;
import com.example.property_management.dto.user.UserResponseDTO;
import com.example.property_management.dto.user.UserUpdateDTO;
import com.example.property_management.entity.AssignmentEntity;
import com.example.property_management.entity.PropertyEntity;
import com.example.property_management.entity.UserEntity;
import com.example.property_management.enums.UserRole;
import com.example.property_management.error.exception.CannotDeleteUserException;
import com.example.property_management.error.exception.UserAlreadyExistsException;
import com.example.property_management.error.exception.UserNotFoundException;
import com.example.property_management.mapper.AssignmentMapper;
import com.example.property_management.mapper.UserMapper;
import com.example.property_management.repository.AssignmentRepository;
import com.example.property_management.repository.PropertyRepository;
import com.example.property_management.repository.UserRepository;
import com.example.property_management.service.AuditLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private AssignmentRepository assignmentRepository;
    @Mock private PropertyRepository propertyRepository;
    @Mock private UserMapper userMapper;
    @Mock private UserAuthorizationService userAuthorizationService;
    @Mock private AssignmentMapper assignmentMapper;
    @Mock private AuditLogService auditLogService;

    @InjectMocks
    private UserServiceImpl userService;

    private UserEntity userEntity;
    private UserResponseDTO userResponseDTO;

    @BeforeEach
    void setUp() {
        userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setFirstName("John");
        userEntity.setLastName("Doe");
        userEntity.setEmail("john@example.com");
        userEntity.setRole(UserRole.GUEST);

        userResponseDTO = new UserResponseDTO();
        userResponseDTO.setId(1L);
        userResponseDTO.setFirstName("John");
        userResponseDTO.setEmail("john@example.com");
    }

    // ---------- getUserById ----------

    @Test
    void getUserById_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        doNothing().when(userAuthorizationService).canGetUser(userEntity);
        when(userMapper.toDTO(userEntity)).thenReturn(userResponseDTO);

        UserResponseDTO result = userService.getUserById(1L);

        assertEquals(1L, result.getId());
        verify(userAuthorizationService).canGetUser(userEntity);
    }

    @Test
    void getUserById_notFound_throws() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserById(99L));
        verify(userAuthorizationService, never()).canGetUser(any());
    }

    // ---------- getAllUsers ----------

    @Test
    void getAllUsers_success() {
        Pageable pageable = PageRequest.of(0, 10);
        PageImpl<UserEntity> page = new PageImpl<>(List.of(userEntity), pageable, 1);

        doNothing().when(userAuthorizationService).canGetAllUsers();
        when(userRepository.findAll(pageable)).thenReturn(page);
        when(userMapper.toDTO(userEntity)).thenReturn(userResponseDTO);

        PageResponse<UserResponseDTO> result = userService.getAllUsers(pageable);

        assertEquals(1, result.content().size());
        assertEquals(1, result.totalElements());
    }

    // ---------- getAssignmentsByUser ----------

    @Test
    void getAssignmentsByUser_success() {
        PropertyEntity property = new PropertyEntity();
        property.setId(2L);

        AssignmentEntity assignmentEntity = new AssignmentEntity();
        assignmentEntity.setId(5L);
        assignmentEntity.setUser(userEntity);
        assignmentEntity.setProperty(property);

        AssignmentDTO assignmentDTO = new AssignmentDTO();
        assignmentDTO.setId(5L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        doNothing().when(userAuthorizationService).canGetAssignmentsByUser(userEntity);
        when(assignmentRepository.findAllByUser_id(1L)).thenReturn(List.of(assignmentEntity));
        when(assignmentMapper.toDTO(assignmentEntity)).thenReturn(assignmentDTO);

        List<AssignmentDTO> result = userService.getAssignmentsByUser(1L);

        assertEquals(1, result.size());
        assertEquals(1L, result.getFirst().getUserId());
        assertEquals(2L, result.getFirst().getPropertyId());
    }

    @Test
    void getAssignmentsByUser_userNotFound_throws() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getAssignmentsByUser(1L));
    }

    // ---------- updateUser ----------

    @Test
    void updateUser_success() {
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setFirstName("Jane");
        dto.setEmail("jane@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        doNothing().when(userAuthorizationService).canUpdateUser(userEntity);
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(userEntity)).thenReturn(userEntity);
        when(userMapper.toDTO(userEntity)).thenReturn(userResponseDTO);

        UserResponseDTO result = userService.updateUser(1L, dto);

        assertNotNull(result);
        verify(userMapper).updateUser(dto, userEntity);
        verify(auditLogService).userLog(eq("User"), eq("1"), eq("Update"), any(), any());
    }

    @Test
    void updateUser_notFound_throws() {
        UserUpdateDTO dto = new UserUpdateDTO();
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.updateUser(1L, dto));
    }

    @Test
    void updateUser_emailBelongsToAnotherUser_throws() {
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setEmail("taken@example.com");

        UserEntity anotherUser = new UserEntity();
        anotherUser.setId(2L);
        anotherUser.setEmail("taken@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        doNothing().when(userAuthorizationService).canUpdateUser(userEntity);
        when(userRepository.findByEmail("taken@example.com")).thenReturn(Optional.of(anotherUser));

        assertThrows(UserAlreadyExistsException.class, () -> userService.updateUser(1L, dto));
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_emailBelongsToSameUser_allowed() {
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setEmail("john@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        doNothing().when(userAuthorizationService).canUpdateUser(userEntity);
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(userEntity));
        when(userRepository.save(userEntity)).thenReturn(userEntity);
        when(userMapper.toDTO(userEntity)).thenReturn(userResponseDTO);

        UserResponseDTO result = userService.updateUser(1L, dto);

        assertNotNull(result);
        verify(userRepository).save(userEntity);
    }

    @Test
    void updateUser_nullEmail_skipsUniquenessCheck() {
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setFirstName("Jane");

        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        doNothing().when(userAuthorizationService).canUpdateUser(userEntity);
        when(userRepository.save(userEntity)).thenReturn(userEntity);
        when(userMapper.toDTO(userEntity)).thenReturn(userResponseDTO);

        userService.updateUser(1L, dto);

        verify(userRepository, never()).findByEmail(any());
    }

    // ---------- deleteUser ----------

    @Test
    void deleteUser_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        doNothing().when(userAuthorizationService).canDeleteUser(userEntity);
        when(propertyRepository.findAllByOwnerId(1L)).thenReturn(List.of());
        when(assignmentRepository.findAllByUser_id(1L)).thenReturn(List.of());

        userService.deleteUser(1L);

        verify(userRepository).delete(userEntity);
        verify(auditLogService).userLog(eq("User"), eq("1"), eq("Delete"), any(), any());
    }

    @Test
    void deleteUser_notFound_throws() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(1L));
    }

    @Test
    void deleteUser_ownerWithProperties_throws() {
        userEntity.setRole(UserRole.OWNER);

        PropertyEntity property = new PropertyEntity();
        property.setId(2L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        doNothing().when(userAuthorizationService).canDeleteUser(userEntity);
        when(propertyRepository.findAllByOwnerId(1L)).thenReturn(List.of(property));

        assertThrows(CannotDeleteUserException.class, () -> userService.deleteUser(1L));
        verify(userRepository, never()).delete(any());
    }

    @Test
    void deleteUser_agentWithAssignments_throws() {
        userEntity.setRole(UserRole.AGENT);

        AssignmentEntity assignment = new AssignmentEntity();
        assignment.setId(3L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        doNothing().when(userAuthorizationService).canDeleteUser(userEntity);
        when(propertyRepository.findAllByOwnerId(1L)).thenReturn(List.of());
        when(assignmentRepository.findAllByUser_id(1L)).thenReturn(List.of(assignment));

        assertThrows(CannotDeleteUserException.class, () -> userService.deleteUser(1L));
        verify(userRepository, never()).delete(any());
    }

    @Test
    void deleteUser_ownerWithNoProperties_succeeds() {
        userEntity.setRole(UserRole.OWNER);

        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        doNothing().when(userAuthorizationService).canDeleteUser(userEntity);
        when(propertyRepository.findAllByOwnerId(1L)).thenReturn(List.of());
        when(assignmentRepository.findAllByUser_id(1L)).thenReturn(List.of());

        userService.deleteUser(1L);

        verify(userRepository).delete(userEntity);
    }
}
