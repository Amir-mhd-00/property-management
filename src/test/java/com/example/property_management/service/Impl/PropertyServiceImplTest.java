package com.example.property_management.service.impl;

import com.example.property_management.authorization.PropertyAuthorizationService;
import com.example.property_management.dto.PageResponse;
import com.example.property_management.dto.assignment.AssignmentDTO;
import com.example.property_management.dto.property.PropertyCreateDTO;
import com.example.property_management.dto.property.PropertyPatchDTO;
import com.example.property_management.dto.property.PropertyResponseDTO;
import com.example.property_management.dto.property.PropertyUpdateDTO;
import com.example.property_management.entity.AssignmentEntity;
import com.example.property_management.entity.PropertyEntity;
import com.example.property_management.entity.UserEntity;
import com.example.property_management.enums.PropertyStatus;
import com.example.property_management.enums.PropertyType;
import com.example.property_management.enums.UserRole;
import com.example.property_management.error.exception.ForbiddenException;
import com.example.property_management.error.exception.PropertyAlreadyExistsException;
import com.example.property_management.error.exception.PropertyNotFoundException;
import com.example.property_management.error.exception.UserNotFoundException;
import com.example.property_management.mapper.AssignmentMapper;
import com.example.property_management.mapper.PropertyMapper;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PropertyServiceImplTest {

    @Mock private PropertyRepository propertyRepository;
    @Mock private AssignmentRepository assignmentRepository;
    @Mock private PropertyMapper propertyMapper;
    @Mock private AssignmentMapper assignmentMapper;
    @Mock private PropertyAuthorizationService propertyAuthorizationService;
    @Mock private UserRepository userRepository;
    @Mock private AuditLogService auditLogService;

    @InjectMocks
    private PropertyServiceImpl propertyService;

    private PropertyEntity propertyEntity;
    private PropertyResponseDTO propertyResponseDTO;
    private UserEntity ownerEntity;

    @BeforeEach
    void setUp() {
        ownerEntity = new UserEntity();
        ownerEntity.setId(10L);
        ownerEntity.setRole(UserRole.OWNER);

        propertyEntity = new PropertyEntity();
        propertyEntity.setId(1L);
        propertyEntity.setPropertyName("Sunset Villa");
        propertyEntity.setLocation("123 Main St");
        propertyEntity.setPropertyStatus(PropertyStatus.AVAILABLE);
        propertyEntity.setPropertyValue(100000.0);
        propertyEntity.setOwner(ownerEntity);

        propertyResponseDTO = new PropertyResponseDTO();
        propertyResponseDTO.setId(1L);
        propertyResponseDTO.setPropertyName("Sunset Villa");
        propertyResponseDTO.setLocation("123 Main St");
        propertyResponseDTO.setPropertyStatus(PropertyStatus.AVAILABLE);
        propertyResponseDTO.setPropertyValue(100000.0);
    }

    // ---------- getProperty ----------

    @Test
    void getProperty_success() {
        when(propertyRepository.findById(1L)).thenReturn(Optional.of(propertyEntity));
        when(propertyMapper.toDTO(propertyEntity)).thenReturn(propertyResponseDTO);

        PropertyResponseDTO result = propertyService.getProperty(1L);

        assertEquals(1L, result.getId());
        assertEquals("Sunset Villa", result.getPropertyName());
        verify(propertyRepository).findById(1L);
    }

    @Test
    void getProperty_notFound_throws() {
        when(propertyRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(PropertyNotFoundException.class, () -> propertyService.getProperty(99L));
        verify(propertyRepository).findById(99L);
    }

    // ---------- createProperty ----------

    @Test
    void createProperty_success() {
        PropertyCreateDTO dto = new PropertyCreateDTO();
        dto.setPropertyName("Sunset Villa");
        dto.setPropertyValue(100000.0);
        dto.setPropertyType(PropertyType.VILLA);
        dto.setPropertyStatus(PropertyStatus.AVAILABLE);
        dto.setLocation("123 Main St");
        dto.setOwnerId(10L);

        doNothing().when(propertyAuthorizationService).canCreateProperty();
        when(propertyRepository.findByPropertyName("Sunset Villa")).thenReturn(Optional.empty());
        when(userRepository.findById(10L)).thenReturn(Optional.of(ownerEntity));
        when(propertyMapper.toEntity(dto)).thenReturn(new PropertyEntity());
        when(propertyRepository.save(any(PropertyEntity.class))).thenReturn(propertyEntity);
        when(propertyMapper.toDTO(propertyEntity)).thenReturn(propertyResponseDTO);

        PropertyResponseDTO result = propertyService.createProperty(dto);

        assertEquals("Sunset Villa", result.getPropertyName());
        assertEquals(10L, result.getOwnerId());
        verify(propertyAuthorizationService).canCreateProperty();
        verify(propertyRepository).save(any(PropertyEntity.class));
        verify(auditLogService).propertyLog(eq("Property"), eq("1"), eq("Create"), any(), any());
    }

    @Test
    void createProperty_alreadyExists_throws() {
        PropertyCreateDTO dto = new PropertyCreateDTO();
        dto.setPropertyName("Sunset Villa");

        doNothing().when(propertyAuthorizationService).canCreateProperty();
        when(propertyRepository.findByPropertyName("Sunset Villa")).thenReturn(Optional.of(propertyEntity));

        assertThrows(PropertyAlreadyExistsException.class, () -> propertyService.createProperty(dto));

        verify(propertyRepository, never()).save(any());
        verify(auditLogService, never()).propertyLog(anyString(), anyString(), anyString(), any(), any());
    }

    @Test
    void createProperty_ownerNotFound_throws() {
        PropertyCreateDTO dto = new PropertyCreateDTO();
        dto.setPropertyName("Sunset Villa");
        dto.setOwnerId(999L);

        doNothing().when(propertyAuthorizationService).canCreateProperty();
        when(propertyRepository.findByPropertyName("Sunset Villa")).thenReturn(Optional.empty());
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> propertyService.createProperty(dto));
        verify(propertyRepository, never()).save(any());
    }

    @Test
    void createProperty_ownerNotOwnerRole_throwsForbidden() {
        PropertyCreateDTO dto = new PropertyCreateDTO();
        dto.setPropertyName("Sunset Villa");
        dto.setOwnerId(10L);

        UserEntity nonOwner = new UserEntity();
        nonOwner.setId(10L);
        nonOwner.setRole(UserRole.GUEST);

        doNothing().when(propertyAuthorizationService).canCreateProperty();
        when(propertyRepository.findByPropertyName("Sunset Villa")).thenReturn(Optional.empty());
        when(userRepository.findById(10L)).thenReturn(Optional.of(nonOwner));

        assertThrows(ForbiddenException.class, () -> propertyService.createProperty(dto));
        verify(propertyRepository, never()).save(any());
    }

    // ---------- getProperties ----------

    @Test
    void getProperties_success() {
        Pageable pageable = PageRequest.of(0, 10);
        PageImpl<PropertyEntity> page = new PageImpl<>(List.of(propertyEntity), pageable, 1);

        when(propertyRepository.findAll(pageable)).thenReturn(page);
        when(propertyMapper.toDTO(propertyEntity)).thenReturn(propertyResponseDTO);

        PageResponse<PropertyResponseDTO> result = propertyService.getProperties(pageable);

        assertEquals(1, result.content().size());
        assertEquals(1, result.totalElements());
        assertTrue(result.first());
        assertTrue(result.last());
    }

    // ---------- updateProperty ----------

    @Test
    void updateProperty_success() {
        Long id = 1L;
        PropertyUpdateDTO dto = new PropertyUpdateDTO(
                "villa", 200000.0, PropertyType.HOUSE, PropertyStatus.AVAILABLE, 4, "456 test Blv");

        when(propertyRepository.findById(id)).thenReturn(Optional.of(propertyEntity));
        doNothing().when(propertyAuthorizationService).canUpdateProperty(id);
        when(propertyRepository.findByPropertyName("villa")).thenReturn(Optional.empty());
        when(propertyMapper.toDTO(propertyEntity)).thenReturn(propertyResponseDTO);

        PropertyResponseDTO result = propertyService.updateProperty(id, dto);

        assertNotNull(result);
        verify(propertyMapper).updateProperty(dto, propertyEntity);
        verify(auditLogService).propertyLog(eq("Property"), eq("1"), eq("PUT"), any(), any());
    }

    @Test
    void updateProperty_notFound_throws() {
        PropertyUpdateDTO dto = new PropertyUpdateDTO(
                "villa", 200000.0, PropertyType.HOUSE, PropertyStatus.AVAILABLE, 4, "456 test Blv");

        when(propertyRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(PropertyNotFoundException.class, () -> propertyService.updateProperty(1L, dto));
        verify(propertyAuthorizationService, never()).canUpdateProperty(any());
    }

    @Test
    void updateProperty_nameAlreadyExistsForAnotherProperty_throws() {
        Long id = 1L;
        PropertyUpdateDTO dto = new PropertyUpdateDTO(
                "otherName", 200000.0, PropertyType.HOUSE, PropertyStatus.AVAILABLE, 4, "456 test Blv");

        PropertyEntity conflicting = new PropertyEntity();
        conflicting.setId(2L);
        conflicting.setPropertyName("otherName");

        when(propertyRepository.findById(id)).thenReturn(Optional.of(propertyEntity));
        doNothing().when(propertyAuthorizationService).canUpdateProperty(id);
        when(propertyRepository.findByPropertyName("otherName")).thenReturn(Optional.of(conflicting));

        assertThrows(PropertyAlreadyExistsException.class, () -> propertyService.updateProperty(id, dto));
        verify(propertyMapper, never()).updateProperty(any(PropertyUpdateDTO.class), any());
    }

    @Test
    void updateProperty_sameNameSameProperty_allowed() {
        Long id = 1L;
        PropertyUpdateDTO dto = new PropertyUpdateDTO(
                "Sunset Villa", 200000.0, PropertyType.HOUSE, PropertyStatus.AVAILABLE, 4, "456 test Blv");

        when(propertyRepository.findById(id)).thenReturn(Optional.of(propertyEntity));
        doNothing().when(propertyAuthorizationService).canUpdateProperty(id);
        when(propertyMapper.toDTO(propertyEntity)).thenReturn(propertyResponseDTO);

        PropertyResponseDTO result = propertyService.updateProperty(id, dto);

        assertNotNull(result);
        verify(propertyRepository, never()).findByPropertyName(anyString());
    }

    // ---------- partialUpdateProperty ----------

    @Test
    void partialUpdateProperty_success() {
        Long id = 1L;
        PropertyPatchDTO dto = new PropertyPatchDTO(
                "newName", null, null, null, null, null);

        when(propertyRepository.findById(id)).thenReturn(Optional.of(propertyEntity));
        doNothing().when(propertyAuthorizationService).canUpdateProperty(id);
        when(propertyRepository.findByPropertyName("newName")).thenReturn(Optional.empty());
        when(propertyMapper.toDTO(propertyEntity)).thenReturn(propertyResponseDTO);

        PropertyResponseDTO result = propertyService.partialUpdateProperty(id, dto);

        assertNotNull(result);
        verify(propertyMapper).updateProperty(dto, propertyEntity);
        verify(auditLogService).propertyLog(eq("Property"), eq("1"), eq("PATCH"), any(), any());
    }

    @Test
    void partialUpdateProperty_nullName_skipsUniquenessCheck() {
        Long id = 1L;
        PropertyPatchDTO dto = new PropertyPatchDTO(
                null, 250000.0, null, null, null, null);

        when(propertyRepository.findById(id)).thenReturn(Optional.of(propertyEntity));
        doNothing().when(propertyAuthorizationService).canUpdateProperty(id);
        when(propertyMapper.toDTO(propertyEntity)).thenReturn(propertyResponseDTO);

        propertyService.partialUpdateProperty(id, dto);

        verify(propertyRepository, never()).findByPropertyName(anyString());
    }

    @Test
    void partialUpdateProperty_nameConflict_throws() {
        Long id = 1L;
        PropertyPatchDTO dto = new PropertyPatchDTO(
                "otherName", null, null, null, null, null);

        PropertyEntity conflicting = new PropertyEntity();
        conflicting.setId(2L);
        conflicting.setPropertyName("otherName");

        when(propertyRepository.findById(id)).thenReturn(Optional.of(propertyEntity));
        doNothing().when(propertyAuthorizationService).canUpdateProperty(id);
        when(propertyRepository.findByPropertyName("otherName")).thenReturn(Optional.of(conflicting));

        assertThrows(PropertyAlreadyExistsException.class, () -> propertyService.partialUpdateProperty(id, dto));
    }

    // ---------- deleteProperty ----------

    @Test
    void deleteProperty_success() {
        doNothing().when(propertyAuthorizationService).canDeleteProperty();
        when(propertyRepository.findById(1L)).thenReturn(Optional.of(propertyEntity));

        propertyService.deleteProperty(1L);

        verify(propertyRepository).delete(propertyEntity);
        verify(auditLogService).propertyLog(eq("Property"), eq("1"), eq("Delete"), any(), any());
    }

    @Test
    void deleteProperty_notFound_throws() {
        doNothing().when(propertyAuthorizationService).canDeleteProperty();
        when(propertyRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(PropertyNotFoundException.class, () -> propertyService.deleteProperty(1L));
        verify(propertyRepository, never()).delete(any());
    }

    // ---------- getAllPropertiesByStatus ----------

    @Test
    void getAllPropertiesByStatus_returnsList() {
        when(propertyRepository.findAllByPropertyStatus(PropertyStatus.AVAILABLE))
                .thenReturn(List.of(propertyEntity));
        when(propertyMapper.toDTO(propertyEntity)).thenReturn(propertyResponseDTO);

        List<PropertyResponseDTO> result = propertyService.getAllPropertiesByStatus(PropertyStatus.AVAILABLE);

        assertEquals(1, result.size());
        assertEquals("Sunset Villa", result.getFirst().getPropertyName());
    }

    @Test
    void getAllPropertiesByStatus_empty() {
        when(propertyRepository.findAllByPropertyStatus(PropertyStatus.SOLD))
                .thenReturn(List.of());

        List<PropertyResponseDTO> result = propertyService.getAllPropertiesByStatus(PropertyStatus.SOLD);

        assertTrue(result.isEmpty());
    }

    // ---------- getAssignmentsByProperty ----------

    @Test
    void getAssignmentsByProperty_success() {
        AssignmentEntity assignmentEntity = new AssignmentEntity();
        assignmentEntity.setId(5L);
        assignmentEntity.setUser(ownerEntity);
        assignmentEntity.setProperty(propertyEntity);

        AssignmentDTO assignmentDTO = new AssignmentDTO();
        assignmentDTO.setId(5L);

        doNothing().when(propertyAuthorizationService).canGetAssignmentsByProperty();
        when(propertyRepository.findById(1L)).thenReturn(Optional.of(propertyEntity));
        when(assignmentRepository.findAllByProperty_id(1L)).thenReturn(List.of(assignmentEntity));
        when(assignmentMapper.toDTO(assignmentEntity)).thenReturn(assignmentDTO);

        List<AssignmentDTO> result = propertyService.getAssignmentsByProperty(1L);

        assertEquals(1, result.size());
        assertEquals(10L, result.getFirst().getUserId());
        assertEquals(1L, result.getFirst().getPropertyId());
    }

    @Test
    void getAssignmentsByProperty_propertyNotFound_throws() {
        doNothing().when(propertyAuthorizationService).canGetAssignmentsByProperty();
        when(propertyRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(PropertyNotFoundException.class, () -> propertyService.getAssignmentsByProperty(1L));
    }
}
