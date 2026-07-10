package com.example.property_management.mapper;

import com.example.property_management.dto.PropertyDTO;
import com.example.property_management.dto.PropertyUpdateDTO;
import com.example.property_management.entity.PropertyEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface PropertyMapper {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateProperty(PropertyUpdateDTO dto,
                        @MappingTarget PropertyEntity entity);

    @Mapping(source = "owner.id", target = "ownerId")
    PropertyDTO toDTO(PropertyEntity property);
}