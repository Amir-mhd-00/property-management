package com.example.property_management.authorization;

import com.example.property_management.entity.AssignmentEntity;
import com.example.property_management.entity.UserEntity;
import com.example.property_management.enums.UserRole;
import com.example.property_management.error.exception.ForbiddenException;
import com.example.property_management.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssignmentAuthorizationServiceTest {

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private AssignmentAuthorizationService assignmentAuthorizationService;

    private AssignmentEntity assignmentEntity;

    @BeforeEach
    void setUp() {
        UserEntity user = new UserEntity();
        user.setId(2L);

        assignmentEntity = new AssignmentEntity();
        assignmentEntity.setId(1L);
        assignmentEntity.setUser(user);
    }

    // ---------- canCreateAssignment ----------

    @Test
    void canCreateAssignment_sufficientRole_allowed() {
        when(securityUtils.getCurrentUserRole()).thenReturn(UserRole.AGENT_ADMIN);

        assertDoesNotThrow(() -> assignmentAuthorizationService.canCreateAssignment());
    }

    @Test
    void canCreateAssignment_insufficientRole_throws() {
        when(securityUtils.getCurrentUserRole()).thenReturn(UserRole.AGENT);

        assertThrows(ForbiddenException.class, () -> assignmentAuthorizationService.canCreateAssignment());
    }

    // ---------- canGetAssignment ----------

    @Test
    void canGetAssignment_insufficientRole_throws() {
        when(securityUtils.getCurrentUserId()).thenReturn(2L);
        when(securityUtils.getCurrentUserRole()).thenReturn(UserRole.OWNER);

        assertThrows(ForbiddenException.class, () -> assignmentAuthorizationService.canGetAssignment(assignmentEntity));
    }

    @Test
    void canGetAssignment_agentViewingOwnAssignment_allowed() {
        when(securityUtils.getCurrentUserId()).thenReturn(2L);
        when(securityUtils.getCurrentUserRole()).thenReturn(UserRole.AGENT);

        assertDoesNotThrow(() -> assignmentAuthorizationService.canGetAssignment(assignmentEntity));
    }

    @Test
    void canGetAssignment_agentViewingOtherAssignment_throws() {
        when(securityUtils.getCurrentUserId()).thenReturn(99L);
        when(securityUtils.getCurrentUserRole()).thenReturn(UserRole.AGENT);

        assertThrows(ForbiddenException.class, () -> assignmentAuthorizationService.canGetAssignment(assignmentEntity));
    }

    @Test
    void canGetAssignment_agentAdmin_canViewAnyAssignment() {
        when(securityUtils.getCurrentUserId()).thenReturn(99L);
        when(securityUtils.getCurrentUserRole()).thenReturn(UserRole.AGENT_ADMIN);

        assertDoesNotThrow(() -> assignmentAuthorizationService.canGetAssignment(assignmentEntity));
    }

    // ---------- canGetAllAssignments ----------

    @Test
    void canGetAllAssignments_sufficientRole_allowed() {
        when(securityUtils.getCurrentUserRole()).thenReturn(UserRole.AGENT_ADMIN);

        assertDoesNotThrow(() -> assignmentAuthorizationService.canGetAllAssignments());
    }

    @Test
    void canGetAllAssignments_insufficientRole_throws() {
        when(securityUtils.getCurrentUserRole()).thenReturn(UserRole.AGENT);

        assertThrows(ForbiddenException.class, () -> assignmentAuthorizationService.canGetAllAssignments());
    }

    // ---------- canEndAssignment ----------

    @Test
    void canEndAssignment_sufficientRole_allowed() {
        when(securityUtils.getCurrentUserRole()).thenReturn(UserRole.MANAGER);

        assertDoesNotThrow(() -> assignmentAuthorizationService.canEndAssignment());
    }

    @Test
    void canEndAssignment_insufficientRole_throws() {
        when(securityUtils.getCurrentUserRole()).thenReturn(UserRole.AGENT);

        assertThrows(ForbiddenException.class, () -> assignmentAuthorizationService.canEndAssignment());
    }
}
