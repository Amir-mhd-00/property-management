package com.example.property_management.ratelimit.service;

import com.example.property_management.ratelimit.config.RateLimitProperties;
import com.example.property_management.ratelimit.model.RateLimitType;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.BucketProxy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.distributed.proxy.RemoteBucketBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisRateLimitServiceTest {

    @Mock private ProxyManager<String> proxyManager;
    @Mock private RemoteBucketBuilder<String> remoteBucketBuilder;
    @Mock private Bucket bucket;

    private RedisRateLimitService redisRateLimitService;

    private RateLimitProperties.Policy policy;

    @BeforeEach
    void setUp() {
        redisRateLimitService = new RedisRateLimitService(proxyManager);

        policy = new RateLimitProperties.Policy();
        policy.setCapacity(5);
        policy.setRefillTokens(5);
        policy.setRefillDuration(Duration.ofMinutes(1));
    }

    @Test
    @SuppressWarnings("unchecked")
    void allowRequest_bucketAllows_returnsTrue() {
        when(proxyManager.builder()).thenReturn(remoteBucketBuilder);
        when(remoteBucketBuilder.build(anyString(), any(Supplier.class))).thenReturn((BucketProxy) bucket);
        when(bucket.tryConsume(1)).thenReturn(true);

        boolean result = redisRateLimitService.allowRequest("USER:1", RateLimitType.LOGIN, policy);

        assertTrue(result);
        verify(bucket).tryConsume(1);
    }

    @Test
    @SuppressWarnings("unchecked")
    void allowRequest_bucketDenies_returnsFalse() {
        when(proxyManager.builder()).thenReturn(remoteBucketBuilder);
        when(remoteBucketBuilder.build(anyString(), any(Supplier.class))).thenReturn((BucketProxy) bucket);
        when(bucket.tryConsume(1)).thenReturn(false);

        boolean result = redisRateLimitService.allowRequest("USER:1", RateLimitType.LOGIN, policy);

        assertFalse(result);
    }

    @Test
    @SuppressWarnings("unchecked")
    void allowRequest_buildsKeyWithTypeAndClientId() {
        when(proxyManager.builder()).thenReturn(remoteBucketBuilder);
        when(remoteBucketBuilder.build(anyString(), any(Supplier.class))).thenReturn((BucketProxy) bucket);
        when(bucket.tryConsume(1)).thenReturn(true);

        redisRateLimitService.allowRequest("USER:7", RateLimitType.SEARCH, policy);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(remoteBucketBuilder).build(keyCaptor.capture(), any(Supplier.class));

        assertEquals("rate_limit:SEARCH:USER:7", keyCaptor.getValue());
    }

    @Test
    @SuppressWarnings("unchecked")
    void allowRequest_configurationSupplierProducesExpectedBandwidth() {
        when(proxyManager.builder()).thenReturn(remoteBucketBuilder);
        when(remoteBucketBuilder.build(anyString(), any(Supplier.class))).thenReturn((BucketProxy) bucket);
        when(bucket.tryConsume(1)).thenReturn(true);

        ArgumentCaptor<Supplier<BucketConfiguration>> supplierCaptor = ArgumentCaptor.forClass(Supplier.class);

        redisRateLimitService.allowRequest("USER:1", RateLimitType.DEFAULT, policy);

        verify(remoteBucketBuilder).build(anyString(), supplierCaptor.capture());

        BucketConfiguration configuration = supplierCaptor.getValue().get();

        assertEquals(1, configuration.getBandwidths().length);
        assertEquals(5, configuration.getBandwidths()[0].getCapacity());
    }
}
