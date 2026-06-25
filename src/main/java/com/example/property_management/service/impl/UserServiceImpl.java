package com.example.property_management.service.impl;

import com.example.property_management.dto.*;
import com.example.property_management.entity.UserEntity;
import com.example.property_management.error.exception.InvalidCredentialsException;
import com.example.property_management.error.exception.UserAlreadyExistsException;
import com.example.property_management.error.exception.UserNotFoundException;
import com.example.property_management.repository.UserRepository;
import com.example.property_management.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Override
    public UserResponseDTO register(RegisterUserDTO dto) {

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new UserAlreadyExistsException("Email Already Exists");
        }

        UserEntity user = new UserEntity();
        BeanUtils.copyProperties(dto, user);

        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        UserEntity savedUser = userRepository.save(user);

        UserResponseDTO response = new UserResponseDTO();
        BeanUtils.copyProperties(savedUser, response);

        return response;
    }

    @Override
    public LoginResponseDTO login(LoginRequestDTO loginRequestDTO) {

        UserEntity user = userRepository.findByEmail(loginRequestDTO.getEmail()).orElseThrow(
                () -> new UserNotFoundException("User not found with email: " + loginRequestDTO.getEmail()));

        if(!passwordEncoder.matches(loginRequestDTO.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid Credentials");
        }

        LoginResponseDTO response = new LoginResponseDTO();
        BeanUtils.copyProperties(user, response);

        return response;
    }
}
