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

        if (uri.equals("/api/v1/auth/login")) {
            return new RateLimitContext(RateLimitType.LOGIN, properties.getLogin());
        }

        if (uri.equals("/api/v1/auth/register")) {
            return new RateLimitContext(RateLimitType.REGISTER, properties.getRegister());
        }

        if (uri.startsWith("/api/v1/search")) {
            return new RateLimitContext(RateLimitType.SEARCH, properties.getSearch());
        }

        return new RateLimitContext(RateLimitType.DEFAULT, properties.getDefaultPolicy());
    }

    public static class RateLimitContext {

        private RateLimitType type;

        private RateLimitProperties.Policy policy;

        public RateLimitContext(RateLimitType type, RateLimitProperties.Policy policy) {
            this.type = type;
            this.policy = policy;
        }

        public RateLimitType getType() {
            return type;
        }

        public RateLimitProperties.Policy getPolicy() {
            return policy;
        }

    }

}