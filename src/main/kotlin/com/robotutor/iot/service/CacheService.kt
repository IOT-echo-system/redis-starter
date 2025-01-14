package com.robotutor.iot.service

import com.robotutor.loggingstarter.logOnError
import com.robotutor.loggingstarter.logOnSuccess
import com.robotutor.loggingstarter.serializer.DefaultSerializer
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration

@Service
class CacheService(private val reactiveRedisTemplate: ReactiveRedisTemplate<String, String>) {
    fun <T : Any> retrieve(
        key: String,
        clazz: Class<T>,
        ttlInSeconds: Long = 600,
        switchIfAbsent: () -> Mono<T>
    ): Mono<T> {
        return getValue(key, clazz)
            .logOnSuccess("Successfully get value for $key")
            .logOnError("", "Failed to get value for $key")
            .switchIfEmpty(
                switchIfAbsent()
                    .flatMap { setValue(key, it, ttlInSeconds) }
                    .logOnSuccess("Successfully set value for $key")
                    .logOnError("", "Failed to set value for $key")
            )
    }

    fun <T : Any> retrieves(
        key: String,
        clazz: Class<T>,
        ttlInSeconds: Long = 600,
        switchIfAbsent: () -> Flux<T>
    ): Flux<T> {
        return getValues(key, clazz)
            .switchIfEmpty(
                switchIfAbsent()
                    .flatMap { setValueInList(key, it, ttlInSeconds) }
            )
    }

    fun <T : Any> update(key: String, ttlInSeconds: Long = 600, switchIfAbsent: () -> Mono<T>): Mono<T> {
        return switchIfAbsent()
            .flatMap { setValue(key, it, ttlInSeconds) }
            .logOnSuccess("Successfully updated value for $key")
            .logOnError("", "Failed to update value for $key")
    }

    fun <T : Any> updates(key: String, ttlInSeconds: Long = 600, switchIfAbsent: () -> Flux<T>): Flux<T> {
        return evictList(key)
            .flatMapMany {
                switchIfAbsent()
                    .flatMap { setValueInList(key, it, ttlInSeconds) }
            }
    }

    fun evict(key: String): Mono<Boolean> {
        return reactiveRedisTemplate.opsForValue().delete(key)
            .logOnSuccess("Successfully clear value for $key")
            .logOnError("", "Failed to clear value for $key")
    }

    fun evictList(key: String): Mono<Boolean> {
        return reactiveRedisTemplate.opsForList().delete(key)
            .logOnSuccess("Successfully clear values from list for $key")
            .logOnError("", "Failed to clear values from list for $key")
    }

    private fun <T : Any> setValue(key: String, value: T, ttlInSeconds: Long = 600): Mono<T> {
        return reactiveRedisTemplate.opsForValue()
            .set(key, DefaultSerializer.serialize(value), Duration.ofSeconds(ttlInSeconds))
            .map { value }
    }

    private fun <T : Any> getValue(key: String, clazz: Class<T>): Mono<T> {
        return reactiveRedisTemplate.opsForValue().get(key).map {
            DefaultSerializer.deserialize(it, clazz)
        }
    }

    private fun <T : Any> setValueInList(key: String, value: T, ttlInSeconds: Long = 600): Mono<T> {
        return reactiveRedisTemplate.opsForList()
            .rightPush(key, DefaultSerializer.serialize(value))
            .flatMap { reactiveRedisTemplate.expire(key, Duration.ofSeconds(ttlInSeconds)) }
            .map { value }
    }

    private fun <T : Any> getValues(key: String, clazz: Class<T>): Flux<T> {
        return reactiveRedisTemplate.opsForList().range(key, 0, -1).map {
            DefaultSerializer.deserialize(it, clazz)
        }
    }
}



