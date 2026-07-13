package com.example.property_management.ratelimit.service;

import com.example.property_management.ratelimit.model.RateLimitPolicy;

public class RedisRateLimitService implements  RateLimitService {
    @Override
    public boolean isAllowed(String key, RateLimitPolicy policy) {
        return false;
    }
}
