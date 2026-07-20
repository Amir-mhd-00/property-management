package com.example.property_management.service.impl;

import com.example.property_management.entity.AuditLogEntity;
import com.example.property_management.repository.AuditLogRepository;
import com.example.property_management.service.AuditLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    private static final Logger logger = LoggerFactory.getLogger(AuditLogServiceImpl.class);

    @Override
    public void propertyLog(String entityName, String entityId, String action,
                    Object oldValue, Object newValue) {
        try {
            AuditLogEntity log = new AuditLogEntity();
            log.setEntityName(entityName);
            log.setEntityId(entityId);
            log.setAction(action);
            log.setPerformedBy(getCurrentUser());
            log.setTimestamp(Instant.now());
            log.setOldValue(oldValue != null ? objectMapper.writeValueAsString((oldValue)) : null);
            log.setNewValue(newValue != null ? objectMapper.writeValueAsString(newValue) : null);
            log.setIpAddress(getCurrentRequestIp());

            auditLogRepository.save(log);
        } catch (Exception e) {
            logger.error("couldn't save the audit log something went wrong ", e);
        }
    }

    @Override
    public void userLog(String entityName, String entityId, String action, Object oldValue, Object newValue) {

        try {
            AuditLogEntity log = new AuditLogEntity();
            log.setEntityName(entityName);
            log.setEntityId(entityId);
            log.setAction(action);
            log.setPerformedBy(getCurrentUser());
            log.setTimestamp(Instant.now());
            log.setOldValue(oldValue != null ? objectMapper.writeValueAsString((oldValue)) : null);
            log.setNewValue(newValue != null ? objectMapper.writeValueAsString(newValue) : null);
            log.setIpAddress(getCurrentRequestIp());

            auditLogRepository.save(log);
        } catch (Exception e) {
            logger.error("couldn't save the audit log something went wrong ", e);
        }
    }

    @Override
    public void assignmentLog(String entityName, String entityId, String action, Object oldValue, Object newValue) {
        
    }

    private String getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "SYSTEM"; //is this working right ?
    }

    private String getCurrentRequestIp() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest().getRemoteAddr() : null;
    }
}