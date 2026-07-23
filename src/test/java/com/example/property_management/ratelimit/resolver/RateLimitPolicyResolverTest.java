package com.example.property_management.ratelimit.resolver;

import com.example.property_management.ratelimit.config.RateLimitProperties;
import com.example.property_management.ratelimit.model.RateLimitType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class RateLimitPolicyResolverTest {

    private RateLimitProperties properties;
    private RateLimitPolicyResolver resolver;

    @BeforeEach
    void setUp() {
        properties = new RateLimitProperties();

        RateLimitProperties.Policy login = new RateLimitProperties.Policy();
        login.setCapacity(5);
        login.setRefillTokens(5);
        login.setRefillDuration(Duration.ofMinutes(1));
        properties.setLogin(login);

        RateLimitProperties.Policy register = new RateLimitProperties.Policy();
        register.setCapacity(3);
        register.setRefillTokens(3);
        register.setRefillDuration(Duration.ofHours(1));
        properties.setRegister(register);

        RateLimitProperties.Policy search = new RateLimitProperties.Policy();
        search.setCapacity(100);
        search.setRefillTokens(100);
        search.setRefillDuration(Duration.ofMinutes(1));
        properties.setSearch(search);

        RateLimitProperties.Policy defaultPolicy = new RateLimitProperties.Policy();
        defaultPolicy.setCapacity(300);
        defaultPolicy.setRefillTokens(300);
        defaultPolicy.setRefillDuration(Duration.ofMinutes(1));
        properties.setDefaultPolicy(defaultPolicy);

        resolver = new RateLimitPolicyResolver(properties);
    }

    @Test
    void resolve_loginUri_returnsLoginPolicy() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");

        RateLimitPolicyResolver.RateLimitContext context = resolver.resolve(request);

        assertEquals(RateLimitType.LOGIN, context.type());
        assertSame(properties.getLogin(), context.policy());
    }

    @Test
    void resolve_registerUri_returnsRegisterPolicy() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/register");

        RateLimitPolicyResolver.RateLimitContext context = resolver.resolve(request);

        assertEquals(RateLimitType.REGISTER, context.type());
        assertSame(properties.getRegister(), context.policy());
    }

    @Test
    void resolve_searchUri_returnsSearchPolicy() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/search/properties");

        RateLimitPolicyResolver.RateLimitContext context = resolver.resolve(request);

        assertEquals(RateLimitType.SEARCH, context.type());
        assertSame(properties.getSearch(), context.policy());
    }

    @Test
    void resolve_otherUri_returnsDefaultPolicy() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/properties");

        RateLimitPolicyResolver.RateLimitContext context = resolver.resolve(request);

        assertEquals(RateLimitType.DEFAULT, context.type());
        assertSame(properties.getDefaultPolicy(), context.policy());
    }

    @Test
    void resolve_similarButNonMatchingLoginUri_returnsDefaultPolicy() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login/extra");

        RateLimitPolicyResolver.RateLimitContext context = resolver.resolve(request);

        assertEquals(RateLimitType.DEFAULT, context.type());
    }
}
