package com.example.property_management.mapper;

import com.example.property_management.dto.user.UserResponseDTO;
import com.example.property_management.dto.user.UserUpdateDTO;
import com.example.property_management.entity.UserEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "properties", ignore = true)
    @Mapping(target = "assignments", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUser(UserUpdateDTO dto,
                    @MappingTarget UserEntity entity);

    UserResponseDTO toDTO(UserEntity entity);
}
