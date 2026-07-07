package com.example.property_management.service.impl;

import com.example.property_management.dto.*;
import com.example.property_management.entity.AssignmentEntity;
import com.example.property_management.entity.UserEntity;
import com.example.property_management.error.exception.UserAlreadyExistsException;
import com.example.property_management.error.exception.UserNotFoundException;
import com.example.property_management.mapper.PropertyMapper;
import com.example.property_management.repository.AssignmentRepository;
import com.example.property_management.repository.UserRepository;
import com.example.property_management.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;
    private final AssignmentRepository assignmentRepository;
    private final PropertyMapper propertyMapper;

    public UserServiceImpl(UserRepository userRepository, AssignmentRepository assignmentRepository, PropertyMapper propertyMapper) {
        this.userRepository = userRepository;
        this.assignmentRepository = assignmentRepository;
        this.propertyMapper = propertyMapper;
    }


    @Override
    public List<AssignmentDTO> findByUser(Long id) {

        userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found"));

        logger.info("Fetching assignments for userId={}", id);

        List<AssignmentEntity> assignmentEntities = assignmentRepository.findAllByUser_id(id);

        logger.info("{} assignments found for user {}", assignmentEntities.size(), id);

        List<AssignmentDTO> response = new ArrayList<>();
        for (AssignmentEntity assignment : assignmentEntities) {
            AssignmentDTO responseDTO = new AssignmentDTO();
            BeanUtils.copyProperties(assignment, responseDTO);
            responseDTO.setUserId(assignment.getUser().getId());
            responseDTO.setPropertyId(assignment.getProperty().getId());
            response.add(responseDTO);
        }

        return response;
    }

    @Override
    public UserResponseDTO updateUser(Long id, UserUpdateDTO userUpdateDTO) {

        UserEntity userEntity = userRepository.findById(id).
                orElseThrow(() -> new UserNotFoundException("User not found"));

        logger.info("'PATCH' Updating property id={}", id);

        propertyMapper.updateUser(userUpdateDTO, userEntity);

        if (userRepository.findByEmail(userUpdateDTO.getEmail())
                .filter(existingUser -> !existingUser.getId().equals(id))
                .isPresent()) {
            throw new UserAlreadyExistsException("Email already exists");
        }

        UserEntity savedUser = userRepository.save(userEntity);

        UserResponseDTO responseDTO = new UserResponseDTO();
        BeanUtils.copyProperties(savedUser, responseDTO);

        return responseDTO;
    }

    @Override
    public void deleteUser(Long id) {
        logger.info("Deleting user with id={}", id);
        userRepository.deleteById(id);
    }

}
