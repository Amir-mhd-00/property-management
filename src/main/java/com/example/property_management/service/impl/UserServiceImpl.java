package com.example.property_management.service.impl;

import com.example.property_management.dto.UserDTO;
import com.example.property_management.entity.UserEntity;
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
    public UserDTO register(UserDTO userDTO) {

        UserEntity user = new UserEntity();
        BeanUtils.copyProperties(userDTO, user);

        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));

        UserEntity savedUser = userRepository.save(user);

        UserDTO response = new UserDTO();
        BeanUtils.copyProperties(savedUser, response);

        return response;
    }

    @Override
    public UserDTO login(String password, String ownerEmail) {
        return null;
    }
}
