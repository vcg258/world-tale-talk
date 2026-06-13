package com.worldtaletalk.story.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class StoryCacheService {
    private final RedisTemplate<String, Object> redisTemplate;

    // 키 prefix 상수값
    private static final String CHOICE_STAT_KEY = "story:session:%d:choices";

    // 레디스 보관 시간
    private static final Duration CACHE_TTL = Duration.ofHours(24); // TODO 일단 24시간 지정 필요하면 수정

    /**
     * 직전 턴 선택지별 스텟 변화량 저장
     * @param sessionId 세션 PK
     * @param choiceChangesByChoices 선택지별 스텟 변화량
     */
    public void saveChoiceStats(Long sessionId, Map<String, Map<String, Integer>> choiceChangesByChoices) {
        String key = CHOICE_STAT_KEY.formatted(sessionId);
        redisTemplate.opsForValue().set(key, choiceChangesByChoices, CACHE_TTL);
        log.info("선택지 스텟 캐시 저장 sessionId : {}", sessionId);
    }

    /**
     * 직전 턴 선택지별 스텟 조회
     * @param sessionId 세션 PK
     * @return 선택지별 스텟 변화 (없다? null)
     */
    @SuppressWarnings("unchecked")
    public Map<String, Map<String, Integer>> getChoiceStats(Long sessionId) {
        String key = CHOICE_STAT_KEY.formatted(sessionId);
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? (Map<String, Map<String, Integer>>) value : null;
    }

    /**
     * 세션 캐시 삭제 (세션 상태가 완결 or 중도 이탈시 제거할때 사용)
     * @param sessionId 세션 PK
     */
    public void deleteSessionCache(Long sessionId) {
        String key = CHOICE_STAT_KEY.formatted(sessionId);
        redisTemplate.delete(key);
        log.info("세션 캐시 삭제 sessionId : {}", sessionId);
    }

}
