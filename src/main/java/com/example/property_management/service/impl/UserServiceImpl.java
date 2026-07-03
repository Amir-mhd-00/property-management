package com.example.property_management.service.impl;

import com.example.property_management.dto.*;
import com.example.property_management.entity.AssignmentEntity;
import com.example.property_management.entity.UserEntity;
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
    private final PasswordEncoder passwordEncoder;
    private final AssignmentRepository assignmentRepository;

    public UserServiceImpl(UserRepository userRepository, AssignmentRepository assignmentRepository) {
        this.userRepository = userRepository;
        this.assignmentRepository = assignmentRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Override
    public UserResponseDTO register(RegisterUserDTO dto) {

        logger.info("Registering user with email = {}", dto.getEmail());

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new UserAlreadyExistsException("Email Already Exists");
        }

        UserEntity user = new UserEntity();
        BeanUtils.copyProperties(dto, user);

        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        UserEntity savedUser = userRepository.save(user);

        logger.info("User registered successfully.   email = {} id = {}",
                savedUser.getEmail(),  savedUser.getId());

        UserResponseDTO response = new UserResponseDTO();
        BeanUtils.copyProperties(savedUser, response);

        return response;
    }

    @Override
    public LoginResponseDTO login(LoginRequestDTO loginRequestDTO) {

        logger.info("Authenticating user with email={}", loginRequestDTO.getEmail());

        UserEntity user = userRepository.findByEmail(loginRequestDTO.getEmail()).orElseThrow(
                () -> new UserNotFoundException("User not found with email: " + loginRequestDTO.getEmail()));

        if(!passwordEncoder.matches(loginRequestDTO.getPassword(), user.getPassword())) {

            logger.warn("Authentication Failed for {}", loginRequestDTO.getEmail());

            throw new InvalidCredentialsException("Invalid Credentials");
        }

        logger.info("User Authenticated successfully id  = {}", user.getId());

        LoginResponseDTO response = new LoginResponseDTO();
        BeanUtils.copyProperties(user, response);

        return response;
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
            response.add(responseDTO);
        }

        return response;
    }

}
