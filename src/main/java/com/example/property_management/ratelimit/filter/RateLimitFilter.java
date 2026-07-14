package com.example.property_management.ratelimit.filter;

import com.example.property_management.ratelimit.resolver.DefaultClientIdentifierResolver;
import com.example.property_management.ratelimit.resolver.RateLimitPolicyResolver;
import com.example.property_management.ratelimit.resolver.RateLimitPolicyResolver.RateLimitContext;
import com.example.property_management.ratelimit.service.RateLimitService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class RateLimitFilter extends OncePerRequestFilter {

    private final DefaultClientIdentifierResolver clientIdentifierResolver;
    private final RateLimitPolicyResolver rateLimitPolicyResolver;
    private final RateLimitService rateLimitService;

    public RateLimitFilter(
            DefaultClientIdentifierResolver clientIdentifierResolver,
            RateLimitPolicyResolver rateLimitPolicyResolver,
            RateLimitService rateLimitService) {

        this.clientIdentifierResolver = clientIdentifierResolver;
        this.rateLimitPolicyResolver = rateLimitPolicyResolver;
        this.rateLimitService = rateLimitService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        RateLimitContext context = rateLimitPolicyResolver.resolve(request);
        String clientId = clientIdentifierResolver.resolve(request);

        boolean allowed = rateLimitService.allowRequest(
                clientId,
                context.getType(),
                context.getPolicy());

        if (!allowed) {
            response.setStatus(429); // HttpServletResponse has no TOO_MANY_REQUESTS constant
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"error\":\"Too many requests\",\"message\":\"Rate limit exceeded, please try again later.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
