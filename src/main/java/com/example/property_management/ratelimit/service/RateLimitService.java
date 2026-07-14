package com.example.property_management.ratelimit.service;

import com.example.property_management.ratelimit.config.RateLimitProperties;
import com.example.property_management.ratelimit.model.RateLimitType;

public interface RateLimitService {

        boolean allowRequest(
                String clientId,
                RateLimitType type,
                RateLimitProperties.Policy policy);

    }
