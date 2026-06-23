package com.example.property_management.service.Impl;

import com.example.property_management.DTO.UserDTO;
import com.example.property_management.entity.UserEntity;
import com.example.property_management.repository.UserRepository;
import com.example.property_management.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDTO register(UserDTO userDTO) {

        UserEntity user = new UserEntity();
        BeanUtils.copyProperties(userDTO, user);

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
