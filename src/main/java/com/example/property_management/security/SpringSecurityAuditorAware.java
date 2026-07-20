package com.example.property_management.security;

import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;
import org.jspecify.annotations.NonNull;
import java.util.Optional;

@Component
public class SpringSecurityAuditorAware implements AuditorAware<Long> {

    @Override
    @NonNull
    public Optional<Long> getCurrentAuditor() {

        return SecurityUtils.getCurrentUserOptional().map(CustomUserDetails::getId);
    }
}