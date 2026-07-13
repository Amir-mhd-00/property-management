package com.example.property_management.ratelimit.resolver;

import com.example.property_management.ratelimit.model.RateLimitType;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;


@Component
public class RateLimitPolicyResolver {

    public RateLimitType resolve(HttpServletRequest request) {

        String uri = request.getRequestURI();
        String method = request.getMethod();

        if (method.equals("POST") && uri.equals("/api/v1/auth/login")) {
            return RateLimitType.LOGIN;
        }

        if (method.equals("POST") && uri.equals("/api/v1/auth/register")) {
            return RateLimitType.REGISTER;
        }

        if (method.equals("GET") && uri.startsWith("/api/v1/properties/search")) {
            return RateLimitType.SEARCH;
        }

        return RateLimitType.DEFAULT;
    }
}