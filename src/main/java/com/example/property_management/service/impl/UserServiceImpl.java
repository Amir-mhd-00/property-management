package com.example.property_management.service.impl;

import com.example.property_management.authorization.UserAuthorizationService;
import com.example.property_management.dto.PageResponse;
import com.example.property_management.dto.assignment.AssignmentDTO;
import com.example.property_management.dto.user.UserResponseDTO;
import com.example.property_management.dto.user.UserUpdateDTO;
import com.example.property_management.entity.AssignmentEntity;
import com.example.property_management.entity.UserEntity;
import com.example.property_management.error.exception.UserAlreadyExistsException;
import com.example.property_management.error.exception.UserNotFoundException;
import com.example.property_management.mapper.AssignmentMapper;
import com.example.property_management.mapper.UserMapper;
import com.example.property_management.repository.AssignmentRepository;
import com.example.property_management.repository.UserRepository;
import com.example.property_management.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Pageable;
import java.util.ArrayList;
import java.util.List;


@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;
    private final AssignmentRepository assignmentRepository;
    private final UserAuthorizationService userAuthorizationService;
    private final UserMapper userMapper;
    private final AssignmentMapper assignmentMapper;

    public UserServiceImpl(UserRepository userRepository, AssignmentRepository assignmentRepository, UserMapper userMapper, UserAuthorizationService userAuthorizationService, AssignmentMapper assignmentMapper) {
        this.userRepository = userRepository;
        this.assignmentRepository = assignmentRepository;
        this.userMapper = userMapper;
        this.userAuthorizationService = userAuthorizationService;
        this.assignmentMapper = assignmentMapper;
    }


    @Override
    public UserResponseDTO getUserById(long id) {

        logger.info("fetching user by id {}", id);

        UserEntity user = userRepository.findById(id).
                orElseThrow(() -> new UserNotFoundException("user not found"));

        logger.info("user found {}", user);

        userAuthorizationService.canGetUser(user);

        return userMapper.toDTO(user);
    }

    @Override
    public PageResponse<UserResponseDTO> getAllUsers(Pageable pageable) {

        userAuthorizationService.canGetAllUsers();

        logger.info("fetching all users");

        Page<UserResponseDTO> users = userRepository.findAll(pageable).map(userMapper::toDTO);

        logger.info("found {} users. ", users.getSize());

        return new PageResponse<>(
                users.getContent(),
                users.getNumber(),
                users.getSize(),
                users.getTotalElements(),
                users.getTotalPages(),
                users.isFirst(),
                users.isLast());
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
            AssignmentDTO responseDTO = assignmentMapper.toDTO(assignment);
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

        userMapper.updateUser(userUpdateDTO, userEntity);

        UserEntity savedUser = userRepository.save(userEntity);

        return userMapper.toDTO(savedUser);
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
