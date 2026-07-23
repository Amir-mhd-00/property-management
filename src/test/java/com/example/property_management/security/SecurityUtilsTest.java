package com.example.property_management.security;

import com.example.property_management.entity.UserEntity;
import com.example.property_management.enums.UserRole;
import com.example.property_management.error.exception.UnauthorizedException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SecurityUtilsTest {

    private final SecurityUtils securityUtils = new SecurityUtils();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private UserEntity buildUser(long id, UserRole role) {
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setRole(role);
        user.setEmail("user" + id + "@example.com");
        return user;
    }

    // ---------- getCurrentUserOptional ----------

    @Test
    void getCurrentUserOptional_noAuthentication_returnsEmpty() {
        SecurityContextHolder.clearContext();

        Optional<CustomUserDetails> result = SecurityUtils.getCurrentUserOptional();

        assertTrue(result.isEmpty());
    }

    @Test
    void getCurrentUserOptional_notAuthenticated_returnsEmpty() {
        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken("user", "pw");
        token.setAuthenticated(false);
        SecurityContextHolder.getContext().setAuthentication(token);

        Optional<CustomUserDetails> result = SecurityUtils.getCurrentUserOptional();

        assertTrue(result.isEmpty());
    }

    @Test
    void getCurrentUserOptional_anonymousToken_returnsEmpty() {
        AnonymousAuthenticationToken anonymousToken = new AnonymousAuthenticationToken(
                "key", "anonymousUser", List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));
        SecurityContextHolder.getContext().setAuthentication(anonymousToken);

        Optional<CustomUserDetails> result = SecurityUtils.getCurrentUserOptional();

        assertTrue(result.isEmpty());
    }

    @Test
    void getCurrentUserOptional_principalNotCustomUserDetails_returnsEmpty() {
        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken("someString", "pw", List.of());
        SecurityContextHolder.getContext().setAuthentication(token);

        Optional<CustomUserDetails> result = SecurityUtils.getCurrentUserOptional();

        assertTrue(result.isEmpty());
    }

    @Test
    void getCurrentUserOptional_validCustomUserDetails_returnsUser() {
        UserEntity user = buildUser(1L, UserRole.ADMIN);
        CustomUserDetails details = new CustomUserDetails(user);
        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(token);

        Optional<CustomUserDetails> result = SecurityUtils.getCurrentUserOptional();

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    // ---------- getCurrentUser ----------

    @Test
    void getCurrentUser_notAuthenticated_throwsUnauthorized() {
        SecurityContextHolder.clearContext();

        assertThrows(UnauthorizedException.class, SecurityUtils::getCurrentUser);
    }

    @Test
    void getCurrentUser_authenticated_returnsUser() {
        UserEntity user = buildUser(5L, UserRole.MANAGER);
        CustomUserDetails details = new CustomUserDetails(user);
        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(token);

        CustomUserDetails result = SecurityUtils.getCurrentUser();

        assertEquals(5L, result.getId());
    }

    // ---------- getCurrentUserId / getCurrentUserRole (instance methods) ----------

    @Test
    void getCurrentUserId_returnsAuthenticatedUserId() {
        UserEntity user = buildUser(7L, UserRole.AGENT);
        CustomUserDetails details = new CustomUserDetails(user);
        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(token);

        assertEquals(7L, securityUtils.getCurrentUserId());
    }

    @Test
    void getCurrentUserRole_returnsAuthenticatedUserRole() {
        UserEntity user = buildUser(7L, UserRole.AGENT_ADMIN);
        CustomUserDetails details = new CustomUserDetails(user);
        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(token);

        assertEquals(UserRole.AGENT_ADMIN, securityUtils.getCurrentUserRole());
    }

    @Test
    void getCurrentUserId_notAuthenticated_throws() {
        SecurityContextHolder.clearContext();

        assertThrows(UnauthorizedException.class, securityUtils::getCurrentUserId);
    }

    @Test
    void getCurrentUserRole_notAuthenticated_throws() {
        SecurityContextHolder.clearContext();

        assertThrows(UnauthorizedException.class, securityUtils::getCurrentUserRole);
    }
}
