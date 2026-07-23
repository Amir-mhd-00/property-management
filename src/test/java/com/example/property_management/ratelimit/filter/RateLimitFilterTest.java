package com.example.property_management.ratelimit.filter;

import com.example.property_management.ratelimit.config.RateLimitProperties;
import com.example.property_management.ratelimit.model.RateLimitType;
import com.example.property_management.ratelimit.resolver.DefaultClientIdentifierResolver;
import com.example.property_management.ratelimit.resolver.RateLimitPolicyResolver;
import com.example.property_management.ratelimit.service.RateLimitService;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitFilterTest {

    @Mock private DefaultClientIdentifierResolver clientIdentifierResolver;
    @Mock private RateLimitPolicyResolver rateLimitPolicyResolver;
    @Mock private RateLimitService rateLimitService;
    @Mock private FilterChain filterChain;

    private RateLimitFilter rateLimitFilter;
    private RateLimitProperties.Policy policy;

    @BeforeEach
    void setUp() {
        rateLimitFilter = new RateLimitFilter(clientIdentifierResolver, rateLimitPolicyResolver, rateLimitService);

        policy = new RateLimitProperties.Policy();
        policy.setCapacity(5);
        policy.setRefillTokens(5);
        policy.setRefillDuration(Duration.ofMinutes(1));
    }

    @Test
    void doFilterInternal_requestAllowed_continuesChain() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/properties");
        MockHttpServletResponse response = new MockHttpServletResponse();

        RateLimitPolicyResolver.RateLimitContext context =
                new RateLimitPolicyResolver.RateLimitContext(RateLimitType.DEFAULT, policy);

        when(rateLimitPolicyResolver.resolve(request)).thenReturn(context);
        when(clientIdentifierResolver.resolve(request)).thenReturn("IP:127.0.0.1");
        when(rateLimitService.allowRequest("IP:127.0.0.1", RateLimitType.DEFAULT, policy)).thenReturn(true);

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertEquals(200, response.getStatus());
    }

    @Test
    void doFilterInternal_requestDenied_returns429AndBlocksChain() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
        MockHttpServletResponse response = new MockHttpServletResponse();

        RateLimitPolicyResolver.RateLimitContext context =
                new RateLimitPolicyResolver.RateLimitContext(RateLimitType.LOGIN, policy);

        when(rateLimitPolicyResolver.resolve(request)).thenReturn(context);
        when(clientIdentifierResolver.resolve(request)).thenReturn("IP:127.0.0.1");
        when(rateLimitService.allowRequest("IP:127.0.0.1", RateLimitType.LOGIN, policy)).thenReturn(false);

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, never()).doFilter(any(), any());
        assertEquals(429, response.getStatus());
        assertEquals("application/json", response.getContentType());
        assertTrue(response.getContentAsString().contains("Too many requests"));
    }
}
