package com.example.property_management.service.impl;

import com.example.property_management.dto.auth.LoginRequestDTO;
import com.example.property_management.dto.auth.LoginResponseDTO;
import com.example.property_management.dto.user.UserRegisterDTO;
import com.example.property_management.dto.user.UserResponseDTO;
import com.example.property_management.entity.UserEntity;
import com.example.property_management.enums.UserRole;
import com.example.property_management.error.exception.UserAlreadyExistsException;
import com.example.property_management.repository.UserRepository;
import com.example.property_management.security.CustomUserDetails;
import com.example.property_management.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationServiceImpl.class);

    public AuthenticationServiceImpl(UserRepository userRepository ,AuthenticationManager authenticationManager, SecurityContextRepository securityContextRepository) {
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Override
    public UserResponseDTO register(UserRegisterDTO dto) {

        logger.info("Registering user with email = {}", dto.getEmail());

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new UserAlreadyExistsException("Email Already Exists");
        }

        UserEntity user = new UserEntity();
        BeanUtils.copyProperties(dto, user);

        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(UserRole.GUEST);

        UserEntity savedUser = userRepository.save(user);

        logger.info("User registered successfully.   email = {} id = {}",
                savedUser.getEmail(),  savedUser.getId());

        UserResponseDTO response = new UserResponseDTO();
        BeanUtils.copyProperties(savedUser, response);
        System.out.println(response.getRole());

        return response;
    }

    public LoginResponseDTO login(LoginRequestDTO request,
                                  HttpServletRequest httpServletRequest,
                                  HttpServletResponse httpServletResponse) {

        logger.info("Authenticating user with email={}", request.getEmail());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        securityContextRepository.saveContext(
                context,
                httpServletRequest,
                httpServletResponse
        );

        CustomUserDetails user =
                (CustomUserDetails) authentication.getPrincipal();

        logger.info("User Authenticated successfully id  = {}", user.getUser().getId());

        return new LoginResponseDTO(
                user.getUser().getId(),
                user.getUser().getFirstName(),
                user.getUser().getLastName(),
                user.getUsername(),
                user.getUser().getRole().name());
    }

}