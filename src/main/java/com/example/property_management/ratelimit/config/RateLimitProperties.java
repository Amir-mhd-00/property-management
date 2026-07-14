package com.example.property_management.ratelimit.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@ConfigurationProperties(prefix = "rate-limit")
@Component
@Getter
@Setter
public class RateLimitProperties {

    private Policy login = new Policy();
    private Policy register = new Policy();
    private Policy search = new Policy();
    private Policy defaultPolicy = new Policy();

    @Getter
    @Setter
    public static class Policy {

        private long capacity;

        private long refillTokens;

        private Duration refillDuration;

    }

}