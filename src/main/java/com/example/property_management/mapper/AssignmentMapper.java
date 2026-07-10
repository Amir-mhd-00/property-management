package com.example.property_management.mapper;

import com.example.property_management.dto.assignment.AssignmentDTO;
import com.example.property_management.entity.AssignmentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AssignmentMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "property.id", target = "propertyId")
    AssignmentDTO toDTO(AssignmentEntity property);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "property", ignore = true)
    AssignmentEntity toEntity(AssignmentEntity property);
}
