package com.example.property_management.ratelimit.resolver;

import com.example.property_management.ratelimit.config.RateLimitProperties;
import com.example.property_management.ratelimit.model.RateLimitType;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;


@Component
public class RateLimitPolicyResolver {

    private final RateLimitProperties properties;

    public RateLimitPolicyResolver(RateLimitProperties properties) {
        this.properties = properties;
    }

    public RateLimitContext resolve(HttpServletRequest request) {

        String uri = request.getRequestURI();

        if (uri.equals("/api/auth/login")) {
            return new RateLimitContext(RateLimitType.LOGIN, properties.getLogin());
        }

        if (uri.equals("/api/auth/register")) {
            return new RateLimitContext(RateLimitType.REGISTER, properties.getRegister());
        }

        if (uri.startsWith("/api/search")) {
            return new RateLimitContext(RateLimitType.SEARCH, properties.getSearch());
        }

        return new RateLimitContext(RateLimitType.DEFAULT, properties.getDefaultPolicy());
    }

    public record RateLimitContext(RateLimitType type, RateLimitProperties.Policy policy) {

    }

}