package com.example.property_management.mapper;

import com.example.property_management.dto.assignment.AssignmentDTO;
import com.example.property_management.dto.assignment.CreateAssignmentRequestDTO;
import com.example.property_management.entity.AssignmentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AssignmentMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "property.id", target = "propertyId")
    AssignmentDTO toDTO(AssignmentEntity assignmentEntity);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "property", ignore = true)
    AssignmentEntity toEntity(AssignmentDTO assignmentDTO);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "property", ignore = true)
    AssignmentEntity toEntity(CreateAssignmentRequestDTO assignmentDTO);

    //C:/Users/lenovo/Desktop/projects/spring boot/property-management/src/main/java/com/example/property_management/mapper/AssignmentMapper.java:[18,22] Unmapped target properties: "createdAt, createdBy, updatedAt, updatedBy".
}
