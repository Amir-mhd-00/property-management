package com.example.property_management.authorization;

import com.example.property_management.enums.UserRole;
import com.example.property_management.error.exception.ForbiddenException;
import com.example.property_management.repository.AssignmentRepository;
import com.example.property_management.repository.PropertyRepository;
import com.example.property_management.security.SecurityUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PropertyAuthorizationServiceTest {

    @Mock private SecurityUtils securityUtils;
    @Mock private PropertyRepository propertyRepository;
    @Mock private AssignmentRepository assignmentRepository;

    @InjectMocks
    private PropertyAuthorizationService propertyAuthorizationService;

    // ---------- canCreateProperty ----------

    @Test
    void canCreateProperty_sufficientRole_allowed() {
        when(securityUtils.getCurrentUserRole()).thenReturn(UserRole.AGENT);

        assertDoesNotThrow(() -> propertyAuthorizationService.canCreateProperty());
    }

    @Test
    void canCreateProperty_insufficientRole_throws() {
        when(securityUtils.getCurrentUserRole()).thenReturn(UserRole.GUEST);

        assertThrows(ForbiddenException.class, () -> propertyAuthorizationService.canCreateProperty());
    }

    // ---------- canUpdateProperty ----------

    @Test
    void canUpdateProperty_guest_throws() {
        when(securityUtils.getCurrentUserRole()).thenReturn(UserRole.GUEST);
        when(securityUtils.getCurrentUserId()).thenReturn(1L);

        assertThrows(ForbiddenException.class, () -> propertyAuthorizationService.canUpdateProperty(10L));
    }

    @Test
    void canUpdateProperty_ownerOfProperty_allowed() {
        when(securityUtils.getCurrentUserRole()).thenReturn(UserRole.OWNER);
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(propertyRepository.existsByIdAndOwnerId(10L, 1L)).thenReturn(true);

        assertDoesNotThrow(() -> propertyAuthorizationService.canUpdateProperty(10L));
    }

    @Test
    void canUpdateProperty_ownerNotOwningProperty_throws() {
        when(securityUtils.getCurrentUserRole()).thenReturn(UserRole.OWNER);
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(propertyRepository.existsByIdAndOwnerId(10L, 1L)).thenReturn(false);

        assertThrows(ForbiddenException.class, () -> propertyAuthorizationService.canUpdateProperty(10L));
    }

    @Test
    void canUpdateProperty_agentAssignedToProperty_allowed() {
        when(securityUtils.getCurrentUserRole()).thenReturn(UserRole.AGENT);
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(assignmentRepository.existsByUserIdAndPropertyId(1L, 10L)).thenReturn(true);

        assertDoesNotThrow(() -> propertyAuthorizationService.canUpdateProperty(10L));
    }

    @Test
    void canUpdateProperty_agentNotAssignedToProperty_throws() {
        when(securityUtils.getCurrentUserRole()).thenReturn(UserRole.AGENT);
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(assignmentRepository.existsByUserIdAndPropertyId(1L, 10L)).thenReturn(false);

        assertThrows(ForbiddenException.class, () -> propertyAuthorizationService.canUpdateProperty(10L));
    }

    @Test
    void canUpdateProperty_manager_allowedWithoutChecks() {
        when(securityUtils.getCurrentUserRole()).thenReturn(UserRole.MANAGER);

        assertDoesNotThrow(() -> propertyAuthorizationService.canUpdateProperty(10L));
    }

    // ---------- canDeleteProperty ----------

    @Test
    void canDeleteProperty_sufficientRole_allowed() {
        when(securityUtils.getCurrentUserRole()).thenReturn(UserRole.MANAGER);

        assertDoesNotThrow(() -> propertyAuthorizationService.canDeleteProperty());
    }

    @Test
    void canDeleteProperty_insufficientRole_throws() {
        when(securityUtils.getCurrentUserRole()).thenReturn(UserRole.AGENT_ADMIN);

        assertThrows(ForbiddenException.class, () -> propertyAuthorizationService.canDeleteProperty());
    }

    // ---------- canGetAssignmentsByProperty ----------

    @Test
    void canGetAssignmentsByProperty_sufficientRole_allowed() {
        when(securityUtils.getCurrentUserRole()).thenReturn(UserRole.AGENT_ADMIN);

        assertDoesNotThrow(() -> propertyAuthorizationService.canGetAssignmentsByProperty());
    }

    @Test
    void canGetAssignmentsByProperty_insufficientRole_throws() {
        when(securityUtils.getCurrentUserRole()).thenReturn(UserRole.AGENT);

        assertThrows(ForbiddenException.class, () -> propertyAuthorizationService.canGetAssignmentsByProperty());
    }
}
