package com.example.property_management.service;

public interface AuditLogService {

    void propertyLog(String entityName, String entityId, String action, Object oldValue, Object newValue);
    void userLog(String entityName, String entityId, String action, Object oldValue, Object newValue);
    void assignmentLog(String entityName, String entityId, String action, Object oldValue, Object newValue);

}
