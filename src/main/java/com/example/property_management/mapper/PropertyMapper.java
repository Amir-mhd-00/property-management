package com.example.property_management.mapper;

import com.example.property_management.dto.PropertyUpdateDTO;
import com.example.property_management.entity.PropertyEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface PropertyMapper {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateProperty(PropertyUpdateDTO dto,
                        @MappingTarget PropertyEntity entity);


}