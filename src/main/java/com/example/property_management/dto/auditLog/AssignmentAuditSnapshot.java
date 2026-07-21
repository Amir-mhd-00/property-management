package com.example.property_management.dto.auditLog;

import com.example.property_management.entity.AssignmentEntity;
import com.example.property_management.enums.AssignmentRole;
import com.example.property_management.enums.AssignmentStatus;

import java.time.LocalDateTime;

public record AssignmentAuditSnapshot (
            Long id,
            PropertyAuditSnapshot property,
            UserAuditSnapshot user,
            AssignmentRole role,
            LocalDateTime endDate,
            AssignmentStatus status
) {
        public static AssignmentAuditSnapshot from(AssignmentEntity p) {
            return new AssignmentAuditSnapshot(
                    p.getId(),
                    PropertyAuditSnapshot.from(p.getProperty()),
                    UserAuditSnapshot.from(p.getUser()),
                    p.getRole(),
                    p.getEndDate(),
                    p.getStatus()
            );
        }
}
