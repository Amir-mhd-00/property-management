package com.example.property_management.authorization;

import com.example.property_management.entity.UserEntity;
import com.example.property_management.enums.UserRole;
import com.example.property_management.error.exception.AssignmentNotFoundException;
import com.example.property_management.error.exception.ForbiddenException;
import com.example.property_management.error.exception.UserNotFoundException;
import com.example.property_management.repository.UserRepository;
import com.example.property_management.security.SecurityUtils;
import org.springframework.stereotype.Service;

@Service
public class UserAuthorizationService {

    private final SecurityUtils securityUtils;
    private final UserRepository userRepository;
    public UserAuthorizationService(SecurityUtils securityUtils, UserRepository userRepository) {
        this.securityUtils = securityUtils;
        this.userRepository = userRepository;
    }

    public void canGetUser(Long targetId) {

        UserRole currentRole = securityUtils.getCurrentUserRole();
        Long currentUserId = securityUtils.getCurrentUserId();

        UserEntity targetUser = userRepository.findById(targetId).
                orElseThrow(() -> new UserNotFoundException("USER_NOT_FOUND"));

        if (currentRole == UserRole.ADMIN) {
            return;
        }

        if (currentRole == UserRole.OWNER
                || currentRole == UserRole.GUEST) {

            if (!currentUserId.equals(targetId)) {
                throw new ForbiddenException("You cannot see another user's information.");
            }

            return;
        }

        if (targetUser.getRole().getLevel() >= currentRole.getLevel() &&
                !currentUserId.equals(targetId)) {

            throw new ForbiddenException("You cannot access other users.");
        }
    }

    public void canGetAllUsers() {

        UserRole currentRole = securityUtils.getCurrentUserRole();

        if (currentRole.getLevel() < UserRole.AGENT_ADMIN.getLevel()) {

            throw new ForbiddenException("Insufficient permissions to view all users.");
        }
    }

    public void canGetAssignmentsByUser(Long targetId){

        UserRole currentRole = securityUtils.getCurrentUserRole();
        Long currentUserId = securityUtils.getCurrentUserId();

        UserEntity targetUser = userRepository.findById(targetId).
                orElseThrow(() -> new UserNotFoundException("USER_NOT_FOUND"));

        if (targetUser.getRole() != UserRole.AGENT) {
            throw new AssignmentNotFoundException("This user cannot have assignments.");
        }

        if (currentRole.getLevel() < UserRole.AGENT.getLevel()) {

            throw new ForbiddenException("you cannot see users assignments.");
        }
        if (currentRole == UserRole.AGENT &&
                !currentUserId.equals(targetId)) {

            throw new ForbiddenException("you cannot see users assignments.");
        }
    }

    public void canUpdateUser(Long targetId) {

        UserRole currentRole = securityUtils.getCurrentUserRole();
        Long currentUserId = securityUtils.getCurrentUserId();

        UserEntity targetUser = userRepository.findById(targetId).
                orElseThrow(() -> new UserNotFoundException("USER_NOT_FOUND"));

        if (currentRole == UserRole.ADMIN) {return;}

        if (currentRole == UserRole.AGENT
                ||currentRole == UserRole.GUEST
                || currentRole == UserRole.OWNER) {

            if (!targetUser.getId().equals(currentUserId)) {
                throw new ForbiddenException("you cannot update other users");
            }

            return;
        }

        if (targetUser.getRole().getLevel() > currentRole.getLevel()) {
            throw new ForbiddenException("you cannot update users above your role");
        }
        if (targetUser.getRole() == currentRole&&
            !targetUser.getId().equals(currentUserId)) {
            throw new ForbiddenException("you cannot update other users");
        }

    }

    public void canDeleteUser(Long targetId) {

        UserRole currentRole = securityUtils.getCurrentUserRole();
        Long currentUserId = securityUtils.getCurrentUserId();

        UserEntity targetUser = userRepository.findById(targetId).
            orElseThrow(() -> new UserNotFoundException("USER_NOT_FOUND"));

        if (currentRole.getLevel() < UserRole.AGENT_ADMIN.getLevel()) {
            throw new ForbiddenException("You do not have permission to delete users.");
        }
        if (currentUserId.equals(targetId)) {
            throw new ForbiddenException("You cannot delete yourself.");
        }
        if (targetUser.getRole().getLevel() >= currentRole.getLevel()) {
            throw new ForbiddenException("Insufficient permissions.");
        }
    }
}
