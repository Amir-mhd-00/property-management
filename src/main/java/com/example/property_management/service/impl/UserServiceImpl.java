package com.example.property_management.service.impl;

import com.example.property_management.authorization.UserAuthorizationService;
import com.example.property_management.dto.*;
import com.example.property_management.entity.AssignmentEntity;
import com.example.property_management.entity.UserEntity;
import com.example.property_management.error.exception.UserAlreadyExistsException;
import com.example.property_management.error.exception.UserNotFoundException;
import com.example.property_management.mapper.UserMapper;
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
    private final UserMapper UserMapper;
    private final UserAuthorizationService userAuthorizationService;

    public UserServiceImpl(UserRepository userRepository, AssignmentRepository assignmentRepository, UserMapper userMapper, UserAuthorizationService userAuthorizationService) {
        this.userRepository = userRepository;
        this.assignmentRepository = assignmentRepository;
        this.UserMapper = userMapper;
        this.userAuthorizationService = userAuthorizationService;
    }


    @Override
    public UserResponseDTO getUserById(long id) {

        logger.info("fetching user by id {}", id);

        UserEntity user = userRepository.findById(id).
                orElseThrow(() -> new UserNotFoundException("user not found"));

        logger.info("user found {}", user);

        userAuthorizationService.canGetUser(user);

        UserResponseDTO userResponseDTO = new UserResponseDTO();
        BeanUtils.copyProperties(user, userResponseDTO);

        return userResponseDTO;
    }

    @Override
    public List<UserResponseDTO> getAllUsers() {

        userAuthorizationService.canGetAllUsers();

        logger.info("fetching all users");

        List<UserEntity> userEntities = userRepository.findAll();

        logger.info("found {} users. ", userEntities.size());

        List<UserResponseDTO> userResponseDTOs = new ArrayList<>();
        for (UserEntity userEntity : userEntities) {
            UserResponseDTO userResponseDTO = new UserResponseDTO();
            BeanUtils.copyProperties(userEntity, userResponseDTO);
            userResponseDTOs.add(userResponseDTO);
        }

        return userResponseDTOs;
    }

    @Override
    public List<AssignmentDTO> getAssignmentsByUser(Long id) {

        UserEntity user = userRepository.findById(id).
                orElseThrow(() -> new UserNotFoundException("User not found"));

        userAuthorizationService.canGetAssignmentsByUser(user);

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

        userAuthorizationService.canUpdateUser(userEntity);

        logger.info("'PATCH' Updating User id={}", id);

        if (userRepository.findByEmail(userUpdateDTO.getEmail())
                .filter(existingUser -> !existingUser.getId().equals(id))
                .isPresent()) {
            throw new UserAlreadyExistsException("Email already exists");
        }

        UserMapper.updateUser(userUpdateDTO, userEntity);

        UserEntity savedUser = userRepository.save(userEntity);

        UserResponseDTO responseDTO = new UserResponseDTO();
        BeanUtils.copyProperties(savedUser, responseDTO);

        return responseDTO;
    }

    @Override
    public void deleteUser(Long id) {

        UserEntity userEntity = userRepository.findById(id).
                orElseThrow(() -> new UserNotFoundException("User not found"));

        userAuthorizationService.canDeleteUser(userEntity);

        logger.info("Deleting user with id={}", id);

        userRepository.delete(userEntity);

        logger.info("Deleted user with id={}", id);
    }

}
