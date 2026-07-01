package com.worldtaletalk.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    /**
     * 페르소나 스텟 관련 레디스 설정
     *
     * @param redisConnectionFactory 레디스 펙토리
     * @return Json / Value 직렬화 역직렬화
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        redisTemplate.setKeySerializer(new StringRedisSerializer()); // 키는 문자열로 직렬화
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer()); // 값은 JSON으로 직렬화/역직렬화 (객체 타입 그대로 저장/복원)
        redisTemplate.setHashKeySerializer(new StringRedisSerializer()); // Hash 필드명도 문자열로 직렬화
        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer()); // Hash 값도 JSON으로 직렬화/역직렬화

        return redisTemplate;
    }
}
