package com.robotutor.iot.config

import com.robotutor.iot.serializer.GsonRedisSerializer
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.interceptor.KeyGenerator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.RedisSerializationContext
import java.time.Duration

@Configuration
@EnableCaching
class RedisCacheConfig(private val connectionFactory: ReactiveRedisConnectionFactory) {
    @Bean
    fun redisCacheManager(): RedisCacheManager {
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

        return RedisCacheManager.builder()
            .cacheDefaults(redisCacheConfiguration)
            .withInitialCacheConfigurations(cacheConfigurations)
            .build()
    }

    @Bean
    fun reactiveRedisTemplate(): ReactiveRedisTemplate<String, Any> {
        val keySerializer = GsonRedisSerializer(String::class.java)
        val valueSerializer = GsonRedisSerializer(Any::class.java)
        val redisSerializationContext = RedisSerializationContext.newSerializationContext<String, Any>()
            .key(keySerializer)
            .value(valueSerializer)
            .hashKey(keySerializer)
            .hashValue(valueSerializer)
            .build()
        return ReactiveRedisTemplate(connectionFactory, redisSerializationContext)
    }

    @Bean
    fun customKeyGenerator(): KeyGenerator {
        return KeyGenerator { _, method, params ->
            method.name + "_" + params.joinToString("_")
        }
    }
}
