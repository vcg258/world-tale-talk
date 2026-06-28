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
    private static final Duration TOKEN_TTL = Duration.ofDays(30); // TODO 필요시 수정

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

    @Override
    public void updateToken(String series, String tokenValue, Date lastUsed) {
        String seriesKey = SERIES_KEY.formatted(series);
        redisTemplate.opsForHash().put(seriesKey, "tokenValue", tokenValue);
        redisTemplate.opsForHash().put(seriesKey, "date", String.valueOf(lastUsed.getTime()));
        redisTemplate.expire(seriesKey, TOKEN_TTL);

    }

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

    @Override
    public void removeUserTokens(String username) {
        String userKey = USER_KEY.formatted(username);
        Set<Object> seriesSet = redisTemplate.opsForSet().members(userKey);

        if (seriesSet != null) {
            seriesSet.forEach(series -> redisTemplate.delete(SERIES_KEY.formatted(series)));
        }
        redisTemplate.delete(userKey);
    }
}
