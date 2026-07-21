package com.worldtaletalk.global.mail;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class SignupEmailOtpRedisRepository {
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String OTP_KEY = "signup:email:otp:%s";
    private static final String VERIFIED_KEY = "signup:email:verified:%s"; // 인증 완료 플래그
    private static final Duration OTP_TTL = Duration.ofMinutes(4); // 만료시간
    private static final Duration VERIFIED_TTL = Duration.ofMinutes(10); // 인증 후 가입 폼 제출까지 인증 여부 캐시값 만료시간

    // OTP 재발송 남발 방지 1분 제한
    private static final String COOLDOWN_KEY = "signup:email:otp:cooldown:%s";
    private static final Duration RESEND_COOLDOWN = Duration.ofSeconds(60);

    /**
     * OTP 캐시값 저장
     *
     * @param email 발송한 이메일
     * @param otp 인증번호
     */
    public void saveOtp(String email, String otp) {
        redisTemplate.opsForValue().set(OTP_KEY.formatted(email), otp, OTP_TTL);
    }

    /**
     * OTP 캐시값 조회
     *
     * @param email 발송한 이메일
     * @return 조회값
     */
    public String getOtp(String email) {
        return (String) redisTemplate.opsForValue().get(OTP_KEY.formatted(email));
    }

    /**
     * OTP 캐시값 삭제
     *
     * @param email 이메일
     */
    public void deleteOtp(String email) {
        redisTemplate.delete(OTP_KEY.formatted(email));
    }

    /**
     * 인증 메일 재발송 쿨타임 (1분)
     *
     * @param email 이메일
     * @return
     */
    public boolean isInCooldown(String email) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(COOLDOWN_KEY.formatted(email)));
    }

    /**
     * 메일 재발송 완료 캐시값
     *
     * @param email
     */
    public void markCooldown(String email) {
        redisTemplate.opsForValue().set(COOLDOWN_KEY.formatted(email), "ACCESS", RESEND_COOLDOWN);
    }

    /**
     * 인증완료 캐시값 저장 (OTP인증이 완료되었을 경우 처리가 끝난뒤 캐시값은 지워버리기 때문에 회원가입 요청을 보낼때 OTP인증이 된건지 아닌지 알 수 없음 그래서 추가함)
     *
     * @param email 이메일
     */
    public void markVerified(String email) {
        redisTemplate.opsForValue().set(VERIFIED_KEY.formatted(email), "ACCESS", VERIFIED_TTL);
    }

    /**
     * 인증 유효성 검사
     *
     * @param email 이메일
     * @return 인증 여부
     */
    public boolean isVerified(String email) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(VERIFIED_KEY.formatted(email)));
    }
}
