package com.trader.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisCfg {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // Key 采用 String 序列化
        template.setKeySerializer(new StringRedisSerializer());
        // Value 采用 JSON 序列化
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        // Hash Key 也采用 String
        template.setHashKeySerializer(new StringRedisSerializer());
        // Hash Value 采用 JSON
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        template.afterPropertiesSet();
        return template;
    }
}