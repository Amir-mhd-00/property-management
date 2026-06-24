package com.example.property_management.mapper;

import com.example.property_management.dto.PropertyDTO;
import com.example.property_management.entity.PropertyEntity;
import org.springframework.beans.BeanUtils;

public class propertyMapper {

    public PropertyEntity toEntity(PropertyDTO propertyDTO) {

        PropertyEntity propertyEntity = new PropertyEntity();

        BeanUtils.copyProperties(propertyDTO, propertyEntity);

        return propertyEntity;
    }
}
