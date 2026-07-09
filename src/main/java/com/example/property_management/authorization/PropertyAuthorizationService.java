package com.example.property_management.authorization;

import com.example.property_management.enums.UserRole;
import com.example.property_management.error.exception.ForbiddenException;
import com.example.property_management.repository.AssignmentRepository;
import com.example.property_management.repository.PropertyRepository;
import com.example.property_management.security.SecurityUtils;
import org.springframework.stereotype.Service;

@Service
public class PropertyAuthorizationService {

    private final SecurityUtils securityUtils;
    private final PropertyRepository propertyRepository;
    private final AssignmentRepository assignmentRepository;
    public PropertyAuthorizationService(SecurityUtils securityUtils,
                                        PropertyRepository propertyRepository,
                                        AssignmentRepository assignmentRepository) {
        this.securityUtils = securityUtils;
        this.propertyRepository = propertyRepository;
        this.assignmentRepository = assignmentRepository;
    }

    public void canCreateProperty () {

        if (currentRole().getLevel() < UserRole.AGENT.getLevel()) {
            throw new ForbiddenException("you are not allowed to create property");
        }
    }

    public void canUpdateProperty (Long targetId) {

        Long currentId = securityUtils.getCurrentUserId();

        if (currentRole() == UserRole.GUEST) {
            throw new ForbiddenException("you are not allowed to update a property");}

        if (currentRole() == UserRole.OWNER &&
                !propertyRepository.existsByIdAndOwnerId(targetId, currentId)) {
            throw new ForbiddenException("you can only update you`re own properties");
        }
        if  (currentRole() == UserRole.AGENT &&
                !assignmentRepository.existsByUserIdAndPropertyId(currentId, targetId)) {
            throw new ForbiddenException("you can only update your assigned properties");
        }
    }

    public void canDeleteProperty () {

        if (currentRole().getLevel() < UserRole.MANAGER.getLevel()) {
            throw new ForbiddenException("you are not allowed to delete a property");}
    }

    public void canGetAssignmentsByProperty() {

        if (currentRole().getLevel() < UserRole.AGENT_ADMIN.getLevel()) {
            throw new ForbiddenException("you are not allowed to get assignments");}
    }


    private UserRole currentRole() {
        return securityUtils.getCurrentUserRole();
    }
}
