package com.robotutor.iot.config

import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration

@Configuration
@EnableCaching
class RedisCacheConfig(private val factory: RedisConnectionFactory) {
    @Bean
    fun cacheManager(): CacheManager {
        val redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10))
            .disableCachingNullValues()
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(StringRedisSerializer())
            )
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(StringRedisSerializer())
            )

        val validationCacheConfiguration = redisCacheConfiguration.entryTtl(Duration.ofMinutes(1))

        return RedisCacheManager.builder(factory)
            .cacheDefaults(redisCacheConfiguration)
            .withCacheConfiguration("validation", validationCacheConfiguration)
            .build()
    }
}
