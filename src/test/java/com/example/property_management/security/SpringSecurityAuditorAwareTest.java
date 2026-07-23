package com.example.property_management.security;

import com.example.property_management.entity.UserEntity;
import com.example.property_management.enums.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpringSecurityAuditorAwareTest {

    private final SpringSecurityAuditorAware auditorAware = new SpringSecurityAuditorAware();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentAuditor_noAuthentication_returnsEmpty() {
        SecurityContextHolder.clearContext();

        Optional<Long> result = auditorAware.getCurrentAuditor();

        assertTrue(result.isEmpty());
    }

    @Test
    void getCurrentAuditor_authenticatedUser_returnsUserId() {
        UserEntity user = new UserEntity();
        user.setId(3L);
        user.setRole(UserRole.ADMIN);

        CustomUserDetails details = new CustomUserDetails(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities()));

        Optional<Long> result = auditorAware.getCurrentAuditor();

        assertTrue(result.isPresent());
        assertEquals(3L, result.get());
    }
}
