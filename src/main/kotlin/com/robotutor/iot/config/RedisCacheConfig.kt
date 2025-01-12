package com.robotutor.iot.config

import com.robotutor.iot.serializer.GsonRedisSerializer
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.cache.RedisCacheWriter
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.serializer.RedisSerializationContext
import java.time.Duration

@Configuration
@EnableCaching
class RedisCacheConfig(private val connectionFactory: RedisConnectionFactory) {
    @Bean
    fun redisCacheManager(): RedisCacheManager {
        val cacheWriter = RedisCacheWriter.nonLockingRedisCacheWriter(connectionFactory as RedisConnectionFactory)
        val redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10))
            .disableCachingNullValues()
            .serializeKeysWith(
                RedisSerializationContext
                    .SerializationPair
                    .fromSerializer(GsonRedisSerializer(String::class.java))
            )
            .serializeValuesWith(
                RedisSerializationContext
                    .SerializationPair
                    .fromSerializer(GsonRedisSerializer(Any::class.java))
            )

        val gatewayCacheConfiguration = redisCacheConfiguration.entryTtl(Duration.ofMinutes(1))
        val cacheConfigurations: Map<String, RedisCacheConfiguration> = mapOf(
            "authGateway" to gatewayCacheConfiguration,
            "policyGateway" to gatewayCacheConfiguration,
            "premisesGateway" to gatewayCacheConfiguration
        )

        return RedisCacheManager.builder(cacheWriter)
            .cacheDefaults(redisCacheConfiguration)
            .withInitialCacheConfigurations(cacheConfigurations)
            .build()
    }
}
