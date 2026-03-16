package com.pitisha.project.mybank.accountservice.config;

import static java.time.Duration.ofSeconds;
import static org.springframework.data.redis.cache.RedisCacheConfiguration.defaultCacheConfig;
import static org.springframework.data.redis.cache.RedisCacheManager.builder;
import static org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair.fromSerializer;

import com.pitisha.project.mybank.accountservice.api.dto.response.AccountPageResponse;
import com.pitisha.project.mybank.accountservice.api.dto.response.AccountResponse;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.json.JsonMapper;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisConfig {

    private static final String ACCOUNT_PAGES = "accountPages";
    private static final String FIND_BY_ID_ACCOUNTS = "findByIdAccounts";

    @Bean
    public CacheManager cacheManager(final RedisConnectionFactory connectionFactory, final JsonMapper jm) {
            final RedisCacheConfiguration defaultConfig = defaultCacheConfig()
                    .entryTtl(ofSeconds(30))
                    .serializeKeysWith(fromSerializer(new StringRedisSerializer()));

            return builder(connectionFactory)
                    .cacheDefaults(defaultConfig)
                    .withInitialCacheConfigurations(redisConfigs(defaultConfig, jm))
                    .transactionAware()
                    .build();
    }

    private Map<String, RedisCacheConfiguration> redisConfigs(final RedisCacheConfiguration defaultConfig, final JsonMapper jm) {
        final Map<String, RedisCacheConfiguration> configs = new HashMap<>();
        configs.put(ACCOUNT_PAGES, defaultConfig.serializeValuesWith(
            fromSerializer(new JacksonJsonRedisSerializer<>(jm, AccountPageResponse.class))
        ));
        configs.put(FIND_BY_ID_ACCOUNTS, defaultConfig.serializeValuesWith(
            fromSerializer(new JacksonJsonRedisSerializer<>(jm, AccountResponse.class))
        ));
        return configs;
    }
}
