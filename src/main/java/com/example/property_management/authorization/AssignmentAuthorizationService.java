package com.example.property_management.authorization;

import com.example.property_management.entity.AssignmentEntity;
import com.example.property_management.enums.UserRole;
import com.example.property_management.error.exception.ForbiddenException;
import com.example.property_management.security.SecurityUtils;
import org.springframework.stereotype.Service;

@Service
public class AssignmentAuthorizationService {

    private final SecurityUtils securityUtils;

    public AssignmentAuthorizationService(SecurityUtils securityUtils) {
        this.securityUtils = securityUtils;
    }

    public void canCreateAssignment () {

        if (currentRole().getLevel() < UserRole.AGENT_ADMIN.getLevel()) {
            throw new ForbiddenException("You are not allowed to create an assignment");
        }
    }

    public void canGetAssignment (AssignmentEntity assignment) {

        Long currentId = securityUtils.getCurrentUserId();

        if (currentRole().getLevel() < UserRole.AGENT.getLevel()) {
            throw new ForbiddenException("You are not allowed to access assignments");
        }
        if (currentRole() == UserRole.AGENT && !assignment.getUser().getId().equals(currentId)) {
            throw new ForbiddenException("You can only access your own assignments");
        }
    }

    public void canGetAllAssignments () {

        if (currentRole().getLevel() < UserRole.AGENT_ADMIN.getLevel()) {
            throw new ForbiddenException("You are not allowed to access assignments");
        }
    }

    public void canEndAssignment () {

        if (currentRole().getLevel() < UserRole.AGENT_ADMIN.getLevel()) {
            throw new ForbiddenException("You are not allowed to end assignments");
        }
    }


    private UserRole currentRole() {
        return securityUtils.getCurrentUserRole();
    }
}
