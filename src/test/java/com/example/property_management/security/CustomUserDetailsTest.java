package com.example.property_management.security;

import com.example.property_management.entity.UserEntity;
import com.example.property_management.enums.UserRole;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CustomUserDetailsTest {

    @Test
    void getAuthorities_returnsRolePrefixedAuthority() {
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setRole(UserRole.MANAGER);
        user.setEmail("manager@example.com");

        CustomUserDetails details = new CustomUserDetails(user);

        assertEquals(1, details.getAuthorities().size());
        assertTrue(details.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER")));
    }

    @Test
    void getUsername_returnsEmail() {
        UserEntity user = new UserEntity();
        user.setEmail("test@example.com");
        user.setRole(UserRole.GUEST);

        CustomUserDetails details = new CustomUserDetails(user);

        assertEquals("test@example.com", details.getUsername());
    }

    @Test
    void getPassword_returnsUserPassword() {
        UserEntity user = new UserEntity();
        user.setPassword("secret");
        user.setRole(UserRole.GUEST);

        CustomUserDetails details = new CustomUserDetails(user);

        assertEquals("secret", details.getPassword());
    }

    @Test
    void getId_and_getRole_and_getUser_delegateToUnderlyingUser() {
        UserEntity user = new UserEntity();
        user.setId(42L);
        user.setRole(UserRole.OWNER);

        CustomUserDetails details = new CustomUserDetails(user);

        assertEquals(42L, details.getId());
        assertEquals(UserRole.OWNER, details.getRole());
        assertSame(user, details.getUser());
    }

    @Test
    void accountFlags_defaultToTrue() {
        UserEntity user = new UserEntity();
        user.setRole(UserRole.GUEST);

        CustomUserDetails details = new CustomUserDetails(user);

        assertTrue(details.isAccountNonExpired());
        assertTrue(details.isAccountNonLocked());
        assertTrue(details.isCredentialsNonExpired());
        assertTrue(details.isEnabled());
    }
}
