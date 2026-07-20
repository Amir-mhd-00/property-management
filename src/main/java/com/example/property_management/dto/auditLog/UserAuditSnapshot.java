package com.example.property_management.dto.auditLog;

import com.example.property_management.entity.UserEntity;
import com.example.property_management.enums.UserRole;

public record UserAuditSnapshot(
        Long id,
        String firstName,
        String lastName,
        UserRole role,
        String email,
        String phone
) {
    public static UserAuditSnapshot from(UserEntity p) {
        return new UserAuditSnapshot(
                p.getId(),
                p.getFirstName(),
                p.getLastName(),
                p.getRole(),
                p.getEmail(),
                p.getPhone()
        );
    }
}