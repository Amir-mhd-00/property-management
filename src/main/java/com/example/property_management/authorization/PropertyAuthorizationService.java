package com.example.property_management.authorization;

import com.example.property_management.enums.UserRole;
import com.example.property_management.error.exception.ForbiddenException;
import com.example.property_management.security.SecurityUtils;
import org.springframework.stereotype.Service;

@Service
public class PropertyAuthorizationService {

    private final SecurityUtils securityUtils;
    public PropertyAuthorizationService(SecurityUtils securityUtils) {this.securityUtils = securityUtils;}

    public void canCreateProperty () {

        UserRole currentRole = securityUtils.getCurrentUserRole();

        if (currentRole.getLevel() < UserRole.AGENT.getLevel()) {
            throw new ForbiddenException("you are not allowed to create property");
        }
    }

    public void canUpdateProperty () {

        UserRole currentRole = securityUtils.getCurrentUserRole();

        if (currentRole == UserRole.GUEST) {throw new ForbiddenException("you are not allowed to update a property");}


    }
}
