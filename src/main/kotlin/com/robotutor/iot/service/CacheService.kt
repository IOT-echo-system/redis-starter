package com.robotutor.iot.service

import com.fasterxml.jackson.core.type.TypeReference
import com.robotutor.iot.utils.createFlux
import com.robotutor.loggingstarter.serializer.DefaultSerializer
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration

@Service
class CacheService(private val reactiveRedisTemplate: ReactiveRedisTemplate<String, String>) {
    fun <T : Any> setValue(key: String, value: T, ttlInSeconds: Long = 600): Mono<T> {
        return reactiveRedisTemplate.opsForValue()
            .set(key, DefaultSerializer.serialize(value), Duration.ofSeconds(ttlInSeconds))
            .map { value }
    }

    fun <T : Any> getValue(key: String, clazz: Class<T>): Mono<T> {
        return reactiveRedisTemplate.opsForValue().get(key).map { DefaultSerializer.deserialize(it, clazz) }
    }

    fun <T : Any> setValues(key: String, value: List<T>, ttlInSeconds: Long = 600): Flux<T> {
        return reactiveRedisTemplate.opsForValue()
            .set(key, DefaultSerializer.serialize(value), Duration.ofSeconds(ttlInSeconds))
            .flatMapMany { createFlux(value) }
    }

    fun <T : Any> getValues(key: String, clazz: Class<T>): Flux<T> {
        val listType = object : TypeReference<List<T>>() {}.type
        return reactiveRedisTemplate.opsForValue().get(key)
            .flatMapMany {
                createFlux(DefaultSerializer.deserialize<List<T>>(it, listType))
            }
    }

    fun evict(key: String): Mono<Boolean> {
        return reactiveRedisTemplate.opsForValue().delete(key)
    }
}



