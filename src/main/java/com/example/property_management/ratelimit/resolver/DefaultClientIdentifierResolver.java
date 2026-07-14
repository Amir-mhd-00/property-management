package com.example.property_management.ratelimit.resolver;

import com.example.property_management.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class DefaultClientIdentifierResolver {

    public String resolve(HttpServletRequest request) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !(authentication.getPrincipal() instanceof CustomUserDetails user)) {

            return "IP:" + request.getRemoteAddr();
        }
        return "USER:" + user.getId();
    }
}