package com.robotutor.iot.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory

@Configuration
class RedisConfig {
    @Value("\${spring.data.redis.host}")
    private lateinit var redisHost: String

    @Value("\${spring.data.redis.port}")
    private lateinit var redisPort: Number

    @Bean
    fun redisConnectionFactory(): ReactiveRedisConnectionFactory {
        val redisConfig = RedisStandaloneConfiguration()
        redisConfig.hostName = redisHost
        redisConfig.port = redisPort.toInt()
        return LettuceConnectionFactory(redisConfig)
    }
}
