package com.example.property_management.ratelimit.resolver;

import com.example.property_management.entity.UserEntity;
import com.example.property_management.enums.UserRole;
import com.example.property_management.security.CustomUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultClientIdentifierResolverTest {

    private final DefaultClientIdentifierResolver resolver = new DefaultClientIdentifierResolver();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void resolve_noAuthentication_returnsIpBasedId() {
        SecurityContextHolder.clearContext();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.5");

        String result = resolver.resolve(request);

        assertEquals("IP:10.0.0.5", result);
    }

    @Test
    void resolve_authenticatedCustomUserDetails_returnsUserBasedId() {
        UserEntity user = new UserEntity();
        user.setId(42L);
        user.setRole(UserRole.AGENT);

        CustomUserDetails details = new CustomUserDetails(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities()));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.5");

        String result = resolver.resolve(request);

        assertEquals("USER:42", result);
    }

    @Test
    void resolve_authenticationWithNonCustomPrincipal_returnsIpBasedId() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("someString", "pw"));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.168.0.1");

        String result = resolver.resolve(request);

        assertEquals("IP:192.168.0.1", result);
    }
}
