package com.example.property_management.security;

import com.example.property_management.enums.UserRole;
import com.example.property_management.error.exception.UnauthorizedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SecurityUtils {

    public static Optional<CustomUserDetails> getCurrentUserOptional() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken
                || !(authentication.getPrincipal() instanceof CustomUserDetails user)) {
            return Optional.empty();
        }
        return Optional.of(user);
    }

    public static CustomUserDetails getCurrentUser() {

        return getCurrentUserOptional()
                .orElseThrow(() -> new UnauthorizedException("UNAUTHORIZED"));
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    public UserRole getCurrentUserRole() {
        return getCurrentUser().getRole();
    }
}