package com.robotutor.iot.serializer

import com.robotutor.loggingstarter.serializer.ObjectMapperCache
import org.springframework.data.redis.serializer.RedisSerializer
import org.springframework.data.redis.serializer.SerializationException
import java.nio.charset.StandardCharsets

class GsonRedisSerializer<T> : RedisSerializer<T> {
    override fun serialize(t: T?): ByteArray? {
        return try {
            t?.let { ObjectMapperCache.objectMapper.toJson(it).toByteArray(StandardCharsets.UTF_8) }
        } catch (e: Exception) {
            throw SerializationException("Error serializing object: $t", e)
        }
    }

    override fun deserialize(bytes: ByteArray?): T? {
        return try {
            bytes?.let {
                ObjectMapperCache.objectMapper.fromJson(String(it, StandardCharsets.UTF_8), Any::class.java) as T
            }
        } catch (e: Exception) {
            throw SerializationException("Error deserializing bytes: ${String(bytes ?: ByteArray(0))}", e)
        }
    }
}
