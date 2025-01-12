package com.robotutor.iot.service

import com.robotutor.loggingstarter.serializer.DefaultSerializer
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.Duration

@Service
class CacheService(private val reactiveRedisTemplate: ReactiveRedisTemplate<String, String>) {
    fun <T : Any> storeWithTTL(key: String, value: T, ttlInSeconds: Long): Mono<Boolean> {
        return reactiveRedisTemplate.opsForValue().set(key, asString(value), Duration.ofSeconds(ttlInSeconds))
    }

    fun <T : Any> getValue(key: String, clazz: Class<T>): Mono<T> {
        return reactiveRedisTemplate.opsForValue().get(key).map { fromString(it, clazz) }
    }

    fun evict(key: String): Mono<Boolean> {
        return reactiveRedisTemplate.opsForValue().delete(key)
    }

    fun <T : Any> storeInHash(key: String, hashKey: String, value: T): Mono<Boolean> {
        return reactiveRedisTemplate.opsForHash<String, String>().put(key, hashKey, asString(value))
    }

    fun <T : Any> getFromHash(key: String, hashKey: String, clazz: Class<T>): Mono<T> {
        return reactiveRedisTemplate.opsForHash<String, String>().get(key, hashKey).map { fromString(it, clazz) }
    }

    fun evictFromHash(key: String): Mono<Boolean> {
        return reactiveRedisTemplate.opsForHash<String, String>().delete(key)
    }

    private fun <T : Any> asString(value: T): String {
        return DefaultSerializer.serialize(value)
    }

    private fun <T> fromString(value: String, clazz: Class<T>): T {
        return DefaultSerializer.deserialize(value, clazz)
    }
}
