package com.example.property_management.authorization;

import com.example.property_management.entity.UserEntity;
import com.example.property_management.enums.UserRole;
import com.example.property_management.error.exception.AssignmentNotFoundException;
import com.example.property_management.error.exception.ForbiddenException;
import com.example.property_management.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAuthorizationServiceTest {

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private UserAuthorizationService userAuthorizationService;

    private UserEntity targetUser;

    @BeforeEach
    void setUp() {
        targetUser = new UserEntity();
        targetUser.setId(2L);
        targetUser.setRole(UserRole.AGENT);
    }

    // ---------- canGetUser ----------

    @Test
    void canGetUser_admin_alwaysAllowed() {
        when(securityUtils.getCurrentUserRole()).thenReturn(UserRole.ADMIN);

        assertDoesNotThrow(() -> userAuthorizationService.canGetUser(targetUser));
    }

    @Test
    void canGetUser_owner_canViewOwnProfile() {
        when(securityUtils.getCurrentUserRole()).thenReturn(UserRole.OWNER);
        when(securityUtils.getCurrentUserId()).thenReturn(2L);
        targetUser.setId(2L);

        assertDoesNotThrow(() -> userAuthorizationService.canGetUser(targetUser));
    }

    @Test
    void canGetUser_owner_cannotViewOtherProfile() {
        when(securityUtils.getCurrentUserRole()).thenReturn(UserRole.OWNER);
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        targetUser.setId(2L);

        assertThrows(ForbiddenException.class, () -> userAuthorizationService.canGetUser(targetUser));
    }

    @Test
    void canGetUser_guest_cannotViewOtherProfile() {
        when(securityUtils.getCurrentUserRole()).thenReturn(UserRole.GUEST);
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        targetUser.setId(2L);

        assertThrows(ForbiddenException.class, () -> userAuthorizationService.canGetUser(targetUser));
    }

    @Test
    void canGetUser_higherRoleViewingLowerRole_allowed() {
        when(securityUtils.getCurrentUserRole()).thenReturn(UserRole.MANAGER);
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        targetUser.setRole(UserRole.AGENT);
        targetUser.setId(2L);

        assertDoesNotThrow(() -> userAuthorizationService.canGetUser(targetUser));
    }

    @Test
    void canGetUser_sameOrHigherLevel_notSelf_throws() {
        when(securityUtils.getCurrentUserRole()).thenReturn(UserRole.AGENT_ADMIN);
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        targetUser.setRole(UserRole.AGENT_ADMIN);
        targetUser.setId(2L);

        assertThrows(ForbiddenException.class, () -> userAuthorizationService.canGetUser(targetUser));
    }

    @Test
    void canGetUser_self_alwaysAllowed() {
        when(securityUtils.getCurrentUserRole()).thenReturn(UserRole.AGENT_ADMIN);
        when(securityUtils.getCurrentUserId()).thenReturn(2L);
        targetUser.setRole(UserRole.AGENT_ADMIN);
        targetUser.setId(2L);

        assertDoesNotThrow(() -> userAuthorizationService.canGetUser(targetUser));
    }

    // ---------- canGetAllUsers ----------

    @Test
    void canGetAllUsers_sufficientRole_allowed() {
        when(securityUtils.getCurrentUserRole()).thenReturn(UserRole.AGENT_ADMIN);

        assertDoesNotThrow(() -> userAuthorizationService.canGetAllUsers());
    }

    @Test
    void canGetAllUsers_insufficientRole_throws() {
        when(securityUtils.getCurrentUserRole()).thenReturn(UserRole.AGENT);

        assertThrows(ForbiddenException.class, () -> userAuthorizationService.canGetAllUsers());
    }

    // ---------- canGetAssignmentsByUser ----------

    @Test
    void canGetAssignmentsByUser_targetNotAgent_throwsAssignmentNotFound() {
        targetUser.setRole(UserRole.GUEST);

        assertThrows(AssignmentNotFoundException.class,
                () -> userAuthorizationService.canGetAssignmentsByUser(targetUser));
    }

    @Test
    void canGetAssignmentsByUser_insufficientRole_throwsForbidden() {
        targetUser.setRole(UserRole.AGENT);
        when(securityUtils.getCurrentUserRole()).thenReturn(UserRole.GUEST);

        assertThrows(ForbiddenException.class,
                () -> userAuthorizationService.canGetAssignmentsByUser(targetUser));
    }

    @Test
    void canGetAssignmentsByUser_agentViewingOwnAssignments_allowed() {
        targetUser.setRole(UserRole.AGENT);
        targetUser.setId(2L);
        when(securityUtils.getCurrentUserRole()).thenReturn(UserRole.AGENT);
        when(securityUtils.getCurrentUserId()).thenReturn(2L);

        assertDoesNotThrow(() -> userAuthorizationService.canGetAssignmentsByUser(targetUser));
    }

    @Test
    void canGetAssignmentsByUser_agentViewingAnotherAgent_throws() {
        targetUser.setRole(UserRole.AGENT);
        targetUser.setId(2L);
        when(securityUtils.getCurrentUserRole()).thenReturn(UserRole.AGENT);
        when(securityUtils.getCurrentUserId()).thenReturn(99L);

        assertThrows(ForbiddenException.class,
                () -> userAuthorizationService.canGetAssignmentsByUser(targetUser));
    }

    @Test
    void canGetAssignmentsByUser_agentAdminViewingAnyAgent_allowed() {
        targetUser.setRole(UserRole.AGENT);
        targetUser.setId(2L);
        when(securityUtils.getCurrentUserRole()).thenReturn(UserRole.AGENT_ADMIN);

        assertDoesNotThrow(() -> userAuthorizationService.canGetAssignmentsByUser(targetUser));
    }

    // ---------- canUpdateUser ----------

    @Test
    void canUpdateUser_admin_alwaysAllowed() {
        when(securityUtils.getCurrentUserRole()).thenReturn(UserRole.ADMIN);

        assertDoesNotThrow(() -> userAuthorizationService.canUpdateUser(targetUser));
    }

    @Test
    void canUpdateUser_lowRoleUpdatingSelf_allowed() {
        targetUser.setId(2L);
        when(securityUtils.getCurrentUserRole()).thenReturn(UserRole.AGENT);
        when(securityUtils.getCurrentUserId()).thenReturn(2L);

        assertDoesNotThrow(() -> userAuthorizationService.canUpdateUser(targetUser));
    }

    @Test
    void canUpdateUser_lowRoleUpdatingOther_throws() {
        targetUser.setId(2L);
        when(securityUtils.getCurrentUserRole()).thenReturn(UserRole.AGENT);
        when(securityUtils.getCurrentUserId()).thenReturn(99L);

        assertThrows(ForbiddenException.class, () -> userAuthorizationService.canUpdateUser(targetUser));
    }

    @Test
    void canUpdateUser_targetHigherRole_throws() {
        targetUser.setRole(UserRole.MANAGER);
        when(securityUtils.getCurrentUserRole()).thenReturn(UserRole.AGENT_ADMIN);

        assertThrows(ForbiddenException.class, () -> userAuthorizationService.canUpdateUser(targetUser));
    }

    @Test
    void canUpdateUser_sameRoleNotSelf_throws() {
        targetUser.setRole(UserRole.AGENT_ADMIN);
        targetUser.setId(2L);
        when(securityUtils.getCurrentUserRole()).thenReturn(UserRole.AGENT_ADMIN);
        when(securityUtils.getCurrentUserId()).thenReturn(1L);

        assertThrows(ForbiddenException.class, () -> userAuthorizationService.canUpdateUser(targetUser));
    }

    @Test
    void canUpdateUser_higherRoleUpdatingLowerRole_allowed() {
        targetUser.setRole(UserRole.AGENT);
        when(securityUtils.getCurrentUserRole()).thenReturn(UserRole.MANAGER);

        assertDoesNotThrow(() -> userAuthorizationService.canUpdateUser(targetUser));
    }

    // ---------- canDeleteUser ----------

    @Test
    void canDeleteUser_insufficientRole_throws() {
        when(securityUtils.getCurrentUserRole()).thenReturn(UserRole.AGENT);

        assertThrows(ForbiddenException.class, () -> userAuthorizationService.canDeleteUser(targetUser));
    }

    @Test
    void canDeleteUser_self_throws() {
        targetUser.setId(1L);
        when(securityUtils.getCurrentUserRole()).thenReturn(UserRole.AGENT_ADMIN);
        when(securityUtils.getCurrentUserId()).thenReturn(1L);

        assertThrows(ForbiddenException.class, () -> userAuthorizationService.canDeleteUser(targetUser));
    }

    @Test
    void canDeleteUser_targetSameOrHigherLevel_throws() {
        targetUser.setId(2L);
        targetUser.setRole(UserRole.MANAGER);
        when(securityUtils.getCurrentUserRole()).thenReturn(UserRole.MANAGER);
        when(securityUtils.getCurrentUserId()).thenReturn(1L);

        assertThrows(ForbiddenException.class, () -> userAuthorizationService.canDeleteUser(targetUser));
    }

    @Test
    void canDeleteUser_success() {
        targetUser.setId(2L);
        targetUser.setRole(UserRole.AGENT);
        when(securityUtils.getCurrentUserRole()).thenReturn(UserRole.MANAGER);
        when(securityUtils.getCurrentUserId()).thenReturn(1L);

        assertDoesNotThrow(() -> userAuthorizationService.canDeleteUser(targetUser));
    }
}
