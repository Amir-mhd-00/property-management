package com.example.property_management.service.impl;

import com.example.property_management.dto.*;
import com.example.property_management.entity.AssignmentEntity;
import com.example.property_management.entity.UserEntity;
import com.example.property_management.enums.UserRole;
import com.example.property_management.error.exception.InvalidCredentialsException;
import com.example.property_management.error.exception.UserAlreadyExistsException;
import com.example.property_management.error.exception.UserNotFoundException;
import com.example.property_management.repository.AssignmentRepository;
import com.example.property_management.repository.UserRepository;
import com.example.property_management.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    public UserServiceImpl(UserRepository userRepository, AssignmentRepository assignmentRepository) {
        this.userRepository = userRepository;
        this.assignmentRepository = assignmentRepository;
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

}
