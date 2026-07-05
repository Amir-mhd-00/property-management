package com.example.property_management.service.impl;

import com.example.property_management.dto.LoginRequestDTO;
import com.example.property_management.dto.LoginResponseDTO;
import com.example.property_management.repository.UserRepository;
import com.example.property_management.security.CustomUserDetails;
import com.example.property_management.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;

    private final UserRepository userRepository;

    public LoginResponseDTO login(LoginRequestDTO request) {

        Authentication authentication =
                authenticationManager.authenticate(

                        new UsernamePasswordAuthenticationToken(

                                request.getEmail(),
                                request.getPassword()
                        )
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        CustomUserDetails user =
                (CustomUserDetails) authentication.getPrincipal();

        return new LoginResponseDTO(
                user.getUser().getId(),
                user.getUser().getFirstName(),
                user.getUser().getLastName(),
                user.getUsername(),
                user.getUser().getRole().name());
    }

    @Override
    public void logout() {

    }
}