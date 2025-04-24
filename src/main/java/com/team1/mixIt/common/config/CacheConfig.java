package com.team1.mixIt.common.config;

import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String EMAIL_VERIFICATION_CACHE_NAME = "emailVerificationCache";

    @Bean
    public JCacheManagerCustomizer cacheManagerCustomizer() {
        return cm -> {
            cm.createCache(EMAIL_VERIFICATION_CACHE_NAME, new MutableConfiguration<>()
                    .setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(Duration.TEN_MINUTES))
                    .setStoreByValue(false)
                    .setStatisticsEnabled(true)
            );
        };
    }
}
