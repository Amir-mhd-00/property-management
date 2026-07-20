package com.example.property_management.dto.auditLog;

import com.example.property_management.entity.PropertyEntity;
import com.example.property_management.enums.PropertyStatus;
import com.example.property_management.enums.PropertyType;

public record PropertyAuditSnapshot(
        Long id,
        String propertyName,
        String location,
        Double propertyValue,
        PropertyStatus propertyStatus,
        PropertyType propertyType,
        Integer rooms,
        Long ownerId
) {
    public static PropertyAuditSnapshot from(PropertyEntity p) {
        return new PropertyAuditSnapshot(
                p.getId(),
                p.getPropertyName(),
                p.getLocation(),
                p.getPropertyValue(),
                p.getPropertyStatus(),
                p.getPropertyType(),
                p.getRooms(),
                p.getOwner() != null ? p.getOwner().getId() : null
        );
    }
}