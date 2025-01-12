package com.robotutor.iot.serializer

import com.robotutor.loggingstarter.serializer.DefaultSerializer
import org.springframework.data.redis.serializer.RedisSerializer
import java.nio.charset.StandardCharsets

class GsonRedisSerializer<T : Any>(private val clazz: Class<T>) : RedisSerializer<T> {
    override fun serialize(value: T?): ByteArray {
        return DefaultSerializer.serialize(value).toByteArray(StandardCharsets.UTF_8)
    }

    override fun deserialize(bytes: ByteArray?): T? {
        return bytes?.let { DefaultSerializer.deserialize(bytes.toString(StandardCharsets.UTF_8), clazz) }
    }
}
