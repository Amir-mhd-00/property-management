package com.example.property_management.repository;

import com.example.property_management.entity.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLogEntity, Long> {
    List<AuditLogEntity> findByEntityNameAndEntityId(String entityName, String entityId);
    List<AuditLogEntity> findByPerformedBy(String performedBy);
}