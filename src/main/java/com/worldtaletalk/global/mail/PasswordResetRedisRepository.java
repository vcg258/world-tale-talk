package com.worldtaletalk.global.mail;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class PasswordResetRedisRepository {
    private final RedisTemplate<String, Object> redisTemplate;

    private static final Duration TOKEN_TTL = Duration.ofMinutes(30);
    private static final String KEY_PREFIX = "password-reset:";

    /**
     * 비밀번호 재설정 링크 캐시값 저장
     *
     * @param token 토큰 (비밀번호 URL)
     * @param userId 회원 PK
     */
    public void saveToken(String token, Long userId) {
        redisTemplate.opsForValue().set(KEY_PREFIX + token, userId, TOKEN_TTL);
    }

    /**
     * 캐시값에서 해당 토큰 값 조회 (회원 PK 검증)
     *
     * @param token 토큰
     * @return 회원 PK
     */
    public Long getUserId(String token) {
        Object value = redisTemplate.opsForValue().get(KEY_PREFIX + token);
        return value != null ? (Long) value : null;
    }

    /**
     * 해당 토큰 캐시값 제거 (사용후 바로 삭제를 위함)
     *
     * @param token 토큰
     */
    public void deleteToken(String token) {
        redisTemplate.delete(KEY_PREFIX + token);
    }
}
