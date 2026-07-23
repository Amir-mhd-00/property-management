package com.example.property_management.service.impl;

import com.example.property_management.authorization.AssignmentAuthorizationService;
import com.example.property_management.dto.PageResponse;
import com.example.property_management.dto.assignment.AssignmentDTO;
import com.example.property_management.dto.assignment.CreateAssignmentRequestDTO;
import com.example.property_management.entity.AssignmentEntity;
import com.example.property_management.entity.PropertyEntity;
import com.example.property_management.entity.UserEntity;
import com.example.property_management.enums.AssignmentRole;
import com.example.property_management.enums.AssignmentStatus;
import com.example.property_management.enums.UserRole;
import com.example.property_management.error.exception.*;
import com.example.property_management.mapper.AssignmentMapper;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssignmentServiceImplTest {

    @Mock private AssignmentRepository assignmentRepository;
    @Mock private PropertyRepository propertyRepository;
    @Mock private UserRepository userRepository;
    @Mock private AssignmentAuthorizationService assignmentAuthorizationService;
    @Mock private AssignmentMapper assignmentMapper;
    @Mock private AuditLogService auditLogService;

    @InjectMocks
    private AssignmentServiceImpl assignmentService;

    private PropertyEntity propertyEntity;
    private UserEntity agentEntity;
    private AssignmentEntity assignmentEntity;
    private CreateAssignmentRequestDTO createDto;

    @BeforeEach
    void setUp() {
        propertyEntity = new PropertyEntity();
        propertyEntity.setId(1L);
        propertyEntity.setPropertyName("Sunset Villa");

        agentEntity = new UserEntity();
        agentEntity.setId(2L);
        agentEntity.setRole(UserRole.AGENT);

        assignmentEntity = new AssignmentEntity();
        assignmentEntity.setId(3L);
        assignmentEntity.setProperty(propertyEntity);
        assignmentEntity.setUser(agentEntity);
        assignmentEntity.setStatus(AssignmentStatus.ACTIVE);
        assignmentEntity.setRole(AssignmentRole.PROPERTY_MANAGER);

        createDto = new CreateAssignmentRequestDTO(
                1L, 2L, AssignmentRole.PROPERTY_MANAGER,
                LocalDate.now(), LocalDate.now().plusMonths(1),
                AssignmentStatus.ACTIVE, false);
    }

    // ---------- createAssignment ----------

    @Test
    void createAssignment_success_noExistingAssignment() {
        doNothing().when(assignmentAuthorizationService).canCreateAssignment();
        when(propertyRepository.findById(1L)).thenReturn(Optional.of(propertyEntity));
        when(userRepository.findById(2L)).thenReturn(Optional.of(agentEntity));
        when(assignmentRepository.findByProperty_idAndStatus(1L, AssignmentStatus.ACTIVE))
                .thenReturn(Optional.empty());
        when(assignmentMapper.toEntity(createDto)).thenReturn(new AssignmentEntity());
        when(assignmentRepository.save(any(AssignmentEntity.class))).thenReturn(assignmentEntity);
        AssignmentDTO mappedDto = new AssignmentDTO();
        when(assignmentMapper.toDTO(assignmentEntity)).thenReturn(mappedDto);

        AssignmentDTO result = assignmentService.createAssignment(createDto);

        assertEquals(2L, result.getUserId());
        assertEquals(1L, result.getPropertyId());
        verify(assignmentRepository).save(any(AssignmentEntity.class));
        verify(auditLogService).assignmentLog(eq("Assignment"), eq("3"), eq("Create"), any(), any());
    }

    @Test
    void createAssignment_propertyNotFound_throws() {
        doNothing().when(assignmentAuthorizationService).canCreateAssignment();
        when(propertyRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(PropertyNotFoundException.class, () -> assignmentService.createAssignment(createDto));
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void createAssignment_userNotFound_throws() {
        doNothing().when(assignmentAuthorizationService).canCreateAssignment();
        when(propertyRepository.findById(1L)).thenReturn(Optional.of(propertyEntity));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> assignmentService.createAssignment(createDto));
    }

    @Test
    void createAssignment_userNotAgent_throwsForbidden() {
        UserEntity nonAgent = new UserEntity();
        nonAgent.setId(2L);
        nonAgent.setRole(UserRole.GUEST);

        doNothing().when(assignmentAuthorizationService).canCreateAssignment();
        when(propertyRepository.findById(1L)).thenReturn(Optional.of(propertyEntity));
        when(userRepository.findById(2L)).thenReturn(Optional.of(nonAgent));

        assertThrows(ForbiddenException.class, () -> assignmentService.createAssignment(createDto));
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void createAssignment_alreadyAssignedToSameAgent_throws() {
        doNothing().when(assignmentAuthorizationService).canCreateAssignment();
        when(propertyRepository.findById(1L)).thenReturn(Optional.of(propertyEntity));
        when(userRepository.findById(2L)).thenReturn(Optional.of(agentEntity));
        when(assignmentRepository.findByProperty_idAndStatus(1L, AssignmentStatus.ACTIVE))
                .thenReturn(Optional.of(assignmentEntity));

        assertThrows(AssignmentAlreadyExistsException.class, () -> assignmentService.createAssignment(createDto));
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void createAssignment_propertyAlreadyAssignedToOtherAgent_notReplacing_throws() {
        UserEntity otherAgent = new UserEntity();
        otherAgent.setId(99L);
        otherAgent.setRole(UserRole.AGENT);

        AssignmentEntity existing = new AssignmentEntity();
        existing.setId(7L);
        existing.setUser(otherAgent);
        existing.setProperty(propertyEntity);
        existing.setStatus(AssignmentStatus.ACTIVE);

        doNothing().when(assignmentAuthorizationService).canCreateAssignment();
        when(propertyRepository.findById(1L)).thenReturn(Optional.of(propertyEntity));
        when(userRepository.findById(2L)).thenReturn(Optional.of(agentEntity));
        when(assignmentRepository.findByProperty_idAndStatus(1L, AssignmentStatus.ACTIVE))
                .thenReturn(Optional.of(existing));

        assertThrows(PropertyAlreadyAssignedException.class, () -> assignmentService.createAssignment(createDto));
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void createAssignment_propertyAlreadyAssignedToOtherAgent_replacing_succeeds() {
        UserEntity otherAgent = new UserEntity();
        otherAgent.setId(99L);
        otherAgent.setRole(UserRole.AGENT);

        AssignmentEntity existing = new AssignmentEntity();
        existing.setId(7L);
        existing.setUser(otherAgent);
        existing.setProperty(propertyEntity);
        existing.setStatus(AssignmentStatus.ACTIVE);

        CreateAssignmentRequestDTO replaceDto = new CreateAssignmentRequestDTO(
                1L, 2L, AssignmentRole.PROPERTY_MANAGER,
                LocalDate.now(), LocalDate.now().plusMonths(1),
                AssignmentStatus.ACTIVE, true);

        doNothing().when(assignmentAuthorizationService).canCreateAssignment();
        when(propertyRepository.findById(1L)).thenReturn(Optional.of(propertyEntity));
        when(userRepository.findById(2L)).thenReturn(Optional.of(agentEntity));
        when(assignmentRepository.findByProperty_idAndStatus(1L, AssignmentStatus.ACTIVE))
                .thenReturn(Optional.of(existing));
        when(assignmentMapper.toEntity(replaceDto)).thenReturn(new AssignmentEntity());
        when(assignmentRepository.save(any(AssignmentEntity.class))).thenReturn(assignmentEntity);
        when(assignmentMapper.toDTO(assignmentEntity)).thenReturn(new AssignmentDTO());

        AssignmentDTO result = assignmentService.createAssignment(replaceDto);

        assertNotNull(result);
        assertEquals(AssignmentStatus.COMPLETED, existing.getStatus());
        assertNotNull(existing.getEndDate());
        verify(assignmentRepository).save(any(AssignmentEntity.class));
    }

    // ---------- findById ----------

    @Test
    void findById_success() {
        when(assignmentRepository.findById(3L)).thenReturn(Optional.of(assignmentEntity));
        doNothing().when(assignmentAuthorizationService).canGetAssignment(assignmentEntity);
        when(assignmentMapper.toDTO(assignmentEntity)).thenReturn(new AssignmentDTO());

        AssignmentDTO result = assignmentService.findById(3L);

        assertEquals(2L, result.getUserId());
        assertEquals(1L, result.getPropertyId());
    }

    @Test
    void findById_notFound_throws() {
        when(assignmentRepository.findById(3L)).thenReturn(Optional.empty());

        assertThrows(AssignmentNotFoundException.class, () -> assignmentService.findById(3L));
    }

    // ---------- findAll ----------

    @Test
    void findAll_success() {
        Pageable pageable = PageRequest.of(0, 10);
        PageImpl<AssignmentEntity> page = new PageImpl<>(List.of(assignmentEntity), pageable, 1);

        doNothing().when(assignmentAuthorizationService).canGetAllAssignments();
        when(assignmentRepository.findAll(pageable)).thenReturn(page);
        when(assignmentMapper.toDTO(assignmentEntity)).thenReturn(new AssignmentDTO());

        PageResponse<AssignmentDTO> result = assignmentService.findAll(pageable);

        assertEquals(1, result.content().size());
        assertEquals(1, result.totalElements());
    }

    // ---------- end ----------

    @Test
    void end_success() {
        doNothing().when(assignmentAuthorizationService).canEndAssignment();
        when(assignmentRepository.findById(3L)).thenReturn(Optional.of(assignmentEntity));
        when(assignmentRepository.save(assignmentEntity)).thenReturn(assignmentEntity);
        when(assignmentMapper.toDTO(assignmentEntity)).thenReturn(new AssignmentDTO());

        AssignmentDTO result = assignmentService.end(3L);

        assertEquals(AssignmentStatus.INACTIVE, assignmentEntity.getStatus());
        assertNotNull(assignmentEntity.getEndDate());
        assertEquals(2L, result.getUserId());
        assertEquals(1L, result.getPropertyId());
        verify(auditLogService).assignmentLog(eq("Assignment"), eq("3"), eq("End"), any(), any());
    }

    @Test
    void end_notFound_throws() {
        doNothing().when(assignmentAuthorizationService).canEndAssignment();
        when(assignmentRepository.findById(3L)).thenReturn(Optional.empty());

        assertThrows(AssignmentNotFoundException.class, () -> assignmentService.end(3L));
    }

    @Test
    void end_alreadyInactive_throws() {
        assignmentEntity.setStatus(AssignmentStatus.INACTIVE);

        doNothing().when(assignmentAuthorizationService).canEndAssignment();
        when(assignmentRepository.findById(3L)).thenReturn(Optional.of(assignmentEntity));

        assertThrows(AssignmentAlreadyInactiveException.class, () -> assignmentService.end(3L));
        verify(assignmentRepository, never()).save(any());
    }
}
