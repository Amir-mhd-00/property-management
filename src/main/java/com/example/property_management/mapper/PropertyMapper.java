package com.example.property_management.mapper;

import com.example.property_management.dto.property.PropertyCreateDTO;
import com.example.property_management.dto.property.PropertyPatchDTO;
import com.example.property_management.dto.property.PropertyResponseDTO;
import com.example.property_management.dto.property.PropertyUpdateDTO;
import com.example.property_management.entity.PropertyEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface PropertyMapper {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateProperty(PropertyUpdateDTO dto,
                        @MappingTarget PropertyEntity entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateProperty(PropertyPatchDTO dto,
                        @MappingTarget PropertyEntity entity);


    @Mapping(source = "owner.id", target = "ownerId")
    PropertyResponseDTO toDTO(PropertyEntity property);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "assignments", ignore = true)
    @Mapping(source = "ownerId", target = "owner.id")
    PropertyEntity toEntity(PropertyCreateDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "assignments", ignore = true)
    @Mapping(target = "owner", ignore = true)
    PropertyEntity toEntity(PropertyPatchDTO dto);

}