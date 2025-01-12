package com.robotutor.iot.config

import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import java.time.Duration

@Configuration
@EnableCaching
class RedisCacheConfig(private val factory: RedisConnectionFactory) {
    @Bean
    fun cacheManager(): CacheManager {
        val redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10))
            .disableCachingNullValues()
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    GenericJackson2JsonRedisSerializer()
                )
            )

        val gatewayCacheConfiguration = redisCacheConfiguration.entryTtl(Duration.ofMinutes(1))
        val cacheConfigurations: Map<String, RedisCacheConfiguration> = mapOf(
            "authGateway" to gatewayCacheConfiguration,
            "policyGateway" to gatewayCacheConfiguration,
            "premisesGateway" to gatewayCacheConfiguration
        )

        return RedisCacheManager.builder(factory)
            .cacheDefaults(redisCacheConfiguration)
            .withInitialCacheConfigurations(cacheConfigurations)
            .build()
    }
}
