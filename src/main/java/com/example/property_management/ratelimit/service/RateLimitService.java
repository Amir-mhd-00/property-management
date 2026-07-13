package com.example.property_management.ratelimit.service;

import com.example.property_management.ratelimit.model.RateLimitPolicy;

public interface RateLimitService {

    boolean isAllowed(String key, RateLimitPolicy policy);
}
