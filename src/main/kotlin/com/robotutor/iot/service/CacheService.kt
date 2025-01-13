package com.robotutor.iot.service

import com.google.gson.reflect.TypeToken
import com.robotutor.iot.utils.createFlux
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
            .switchIfEmpty(
                switchIfAbsent()
                    .flatMap { setValue(key, it, ttlInSeconds) }
            )
    }

    fun <T : Any> retrieves(key: String, ttlInSeconds: Long = 600, switchIfAbsent: () -> Flux<T>): Flux<T> {
        return getValues<T>(key)
            .switchIfEmpty(
                switchIfAbsent()
                    .collectList()
                    .flatMapMany { setValues(key, it, ttlInSeconds) }
            )
    }

    fun <T : Any> update(key: String, ttlInSeconds: Long = 600, switchIfAbsent: () -> Mono<T>): Mono<T> {
        return evict(key)
            .flatMap {
                switchIfAbsent()
                    .flatMap { setValue(key, it, ttlInSeconds) }
            }
    }

    fun <T : Any> updates(key: String, ttlInSeconds: Long = 600, switchIfAbsent: () -> Flux<T>): Flux<T> {
        return evict(key)
            .flatMapMany {
                switchIfAbsent()
                    .collectList()
                    .flatMapMany { setValues(key, it, ttlInSeconds) }
            }
    }

    fun evict(key: String): Mono<Boolean> {
        return reactiveRedisTemplate.opsForValue().delete(key)
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

    private fun <T : Any> setValues(key: String, value: List<T>, ttlInSeconds: Long = 600): Flux<T> {
        return reactiveRedisTemplate.opsForValue()
            .set(key, DefaultSerializer.serialize(value), Duration.ofSeconds(ttlInSeconds))
            .flatMapMany { createFlux(value) }
    }

    private fun <T : Any> getValues(key: String): Flux<T> {
        val listType = object : TypeToken<List<T>>() {}.type
        return reactiveRedisTemplate.opsForValue().get(key)
            .flatMapMany {
                createFlux(DefaultSerializer.deserialize<List<T>>(it, listType))
            }
    }
}



