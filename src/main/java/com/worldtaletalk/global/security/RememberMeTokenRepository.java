package com.worldtaletalk.global.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Date;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class RememberMeTokenRepository implements PersistentTokenRepository {
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String SERIES_KEY = "remember-me:series:%s"; // 식별자 (탈취당했을시 구분하기 위함 전체 기간 동안 바뀌지 않는 고정값임)
    private static final String USER_KEY   = "remember-me:user:%s";
    private static final String PROVIDER_FIELD = "provider"; // 제공사 (여러 제공사 연동을 했을 경우 특정하기 위함)
    private static final Duration TOKEN_TTL = Duration.ofDays(30);

    // 일반 로그인(loginId)과 소셜 로그인(userId)이 같은 USER_KEY 포맷을 공유해서 생기는 충돌 방지용 프리픽스
    private static final String OAUTH2_PREFIX = "oauth2:";

    /**
     * 소셜 로그인 유저의 PK를 리멤버미 username 필드용으로 인코딩 (일반 로그인 loginId와 네임스페이스 분리)
     *
     * @param userId 유저 PK
     * @return "oauth2:{userId}" 형태의 인코딩된 문자열
     */
    public static String encodeOAuth2Username(Long userId) {
        return OAUTH2_PREFIX + userId;
    }

    /**
     * 리멤버미에 저장된 username 필드에서 소셜 로그인 유저 PK 복원
     * 프리픽스 적용 전 발급된 기존 쿠키(순수 숫자 문자열)도 호환되도록 처리
     *
     * @param username 저장된 username 값
     * @return 유저 PK
     */
    public static Long decodeOAuth2UserId(String username) {
        if (username.startsWith(OAUTH2_PREFIX)) {
            return Long.parseLong(username.substring(OAUTH2_PREFIX.length()));
        }
        return Long.parseLong(username); // 프리픽스 적용 전 기존 쿠키 호환
    }

    /**
     * RememberMeToken 생성
     *
     * @param token 토큰
     */
    @Override
    public void createNewToken(PersistentRememberMeToken token) {
        String seriesKey = SERIES_KEY.formatted(token.getSeries());
        String userKey = USER_KEY.formatted(token.getUsername());

        Map<String, Object> tokenMap = Map.of(
                "username", token.getUsername(),
                "series", token.getSeries(),
                "tokenValue", token.getTokenValue(),
                "date", String.valueOf(token.getDate().getTime())
        );

        redisTemplate.opsForHash().putAll(seriesKey, tokenMap);
        redisTemplate.expire(seriesKey, TOKEN_TTL);

        redisTemplate.opsForSet().add(userKey, token.getSeries());
        redisTemplate.expire(userKey, TOKEN_TTL);
    }

    /**
     * RememberMeToken 갱신
     *
     * @param series 식별자
     * @param tokenValue 토큰 값
     * @param lastUsed 갱신일
     */
    @Override
    public void updateToken(String series, String tokenValue, Date lastUsed) {
        String seriesKey = SERIES_KEY.formatted(series);

        // userKey 갱신을 위한 기존 토큰 조회
        Map<Object, Object> existing = redisTemplate.opsForHash().entries(seriesKey);
        if (!existing.isEmpty()) {
            String username = (String) existing.get("username");
            String userKey = USER_KEY.formatted(username);
            redisTemplate.expire(userKey, TOKEN_TTL);
        }

        redisTemplate.opsForHash().put(seriesKey, "tokenValue", tokenValue);
        redisTemplate.opsForHash().put(seriesKey, "date", String.valueOf(lastUsed.getTime()));
        redisTemplate.expire(seriesKey, TOKEN_TTL);
    }

    /**
     * 식별자로 rememberMeToken 조회
     *
     * @param seriesId 식별자 ID
     * @return 자동로그인 토큰
     */
    @Override
    public PersistentRememberMeToken getTokenForSeries(String seriesId) {
        String seriesKey = SERIES_KEY.formatted(seriesId);
        Map<Object, Object> tokenMap = redisTemplate.opsForHash().entries(seriesKey);

        if (tokenMap.isEmpty()) return null;

        return new PersistentRememberMeToken(
                (String) tokenMap.get("username"),
                (String) tokenMap.get("series"),
                (String) tokenMap.get("tokenValue"),
                new Date(Long.parseLong((String) tokenMap.get("date")))
        );
    }

    /**
     * RememberMeToken 제거
     *
     * @param username 일반 로그인은 loginId, 소셜 로그인은 oauth2 프리픽스가 붙은 PK
     */
    @Override
    public void removeUserTokens(String username) {
        String userKey = USER_KEY.formatted(username);
        Set<Object> seriesSet = redisTemplate.opsForSet().members(userKey);

        if (seriesSet != null) {
            seriesSet.forEach(series -> redisTemplate.delete(SERIES_KEY.formatted(series)));
        }
        redisTemplate.delete(userKey);
    }

    /**
     * rememberMeToken에 제공사 필드 추가
     *
     * @param series 식별자
     * @param provider 제공사
     */
    public void attachProvider(String series, String provider) {
        String seriesKey = SERIES_KEY.formatted(series);
        redisTemplate.opsForHash().put(seriesKey, PROVIDER_FIELD, provider);
    }

    /**
     * 제공사 조회
     *
     * @param series 식별자
     * @return 해당 식별자의 제공사
     */
    public String getProvider(String series) {
        String seriesKey = SERIES_KEY.formatted(series);
        Object provider = redisTemplate.opsForHash().get(seriesKey, PROVIDER_FIELD);
        return provider != null ? provider.toString() : null;
    }
}
