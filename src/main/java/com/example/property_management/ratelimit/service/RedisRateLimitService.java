package com.example.property_management.ratelimit.service;

import com.example.property_management.ratelimit.config.RateLimitProperties;
import com.example.property_management.ratelimit.model.RateLimitType;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import org.springframework.stereotype.Service;

@Service
public class RedisRateLimitService implements RateLimitService {

    private final ProxyManager<String> proxyManager;

    public RedisRateLimitService(ProxyManager<String> proxyManager) {
        this.proxyManager = proxyManager;
    }

    @Override
    public boolean allowRequest(
            String clientId,
            RateLimitType type,
            RateLimitProperties.Policy policy) {

        String key = buildKey(clientId, type);

        Bucket bucket = proxyManager.builder()
                .build(key, () -> createConfiguration(policy));

        return bucket.tryConsume(1);
    }

        private String buildKey(
            String clientId,
            RateLimitType type) {

        return "rate_limit:" + type.name() + ":" + clientId;
    }

    private BucketConfiguration createConfiguration(
            RateLimitProperties.Policy policy) {

        Bandwidth bandwidth = Bandwidth.builder()
                .capacity(policy.getCapacity())
                .refillGreedy(
                        policy.getRefillTokens(),
                        policy.getRefillDuration())
                .build();

        return BucketConfiguration.builder()
                .addLimit(bandwidth)
                .build();
    }
}

