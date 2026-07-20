package com.example.property_management.dto.auditLog;

import com.example.property_management.entity.AssignmentEntity;
import com.example.property_management.entity.PropertyEntity;
import com.example.property_management.entity.UserEntity;
import com.example.property_management.enums.AssignmentRole;
import com.example.property_management.enums.AssignmentStatus;

import java.time.LocalDateTime;

public record AssignmentAuditSnapshot (
            Long id,
            PropertyEntity property,
            UserEntity user,
            AssignmentRole role,
            LocalDateTime endDate,
            AssignmentStatus status
) {
        public static AssignmentAuditSnapshot from(AssignmentEntity p) {
            return new AssignmentAuditSnapshot(
                    p.getId(),
                    p.getProperty(),
                    p.getUser(),
                    p.getRole(),
                    p.getEndDate(),
                    p.getStatus()
            );
        }
}
