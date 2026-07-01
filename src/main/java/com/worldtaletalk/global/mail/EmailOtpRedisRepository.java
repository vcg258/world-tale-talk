package com.worldtaletalk.global.mail;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class EmailOtpRedisRepository {
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String OTP_KEY = "email:otp:%d";
    private static final Duration OTP_TTL = Duration.ofMinutes(4); // TODO OTP 4분으로 지정 필요하면 수정

    // OTP 재발송 남발 방지 1분 제한
    private static final String COOLDOWN_KEY = "email:otp:cooldown:%d";
    private static final Duration RESEND_COOLDOWN = Duration.ofSeconds(60);

    /**
     * 이메일 변경시 OTP 저장
     *
     * @param userId 회원 PK
     * @param newEmail 변경할 이메일
     * @param code OTP
     */
    public void saveOtp(Long userId, String newEmail, String code) {
        String key = OTP_KEY.formatted(userId);
        redisTemplate.opsForHash().put(key, "email", newEmail);
        redisTemplate.opsForHash().put(key, "code", code);
        redisTemplate.expire(key, OTP_TTL);
    }

    /**
     * 해당 이메일 OTP 검증 및 조회
     *
     * @param userId 회원 PK
     * @param email 이메일
     * @return OTP
     */
    public String getOtpCode(Long userId, String email) {
        String key = OTP_KEY.formatted(userId);
        Object storedEmail = redisTemplate.opsForHash().get(key, "email");
        if (storedEmail == null || !storedEmail.equals(email)) return null; // 발송했던 이메일과 다르면 무효처리
        Object code = redisTemplate.opsForHash().get(key, "code");
        return code != null ? (String) code : null;
    }

    /**
     * OTP 발송후 쿨타임 여부
     *
     * @param userId 회원 PK
     * @return 쿨타임 여부
     */
    public boolean isInCoolDown(Long userId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(COOLDOWN_KEY.formatted(userId)));
    }

    /**
     * OTP 쿨타임 캐시 저장
     *
     * @param userId 회원 PK
     */
    public void markCooldown(Long userId) {
        redisTemplate.opsForValue().set(COOLDOWN_KEY.formatted(userId), "1", RESEND_COOLDOWN);
    }

    /**
     * OTP 캐시 삭제
     *
     * @param userId 회원 PK
     */
    public void deleteOtp(Long userId) {
        redisTemplate.delete(OTP_KEY.formatted(userId));
    }
}
