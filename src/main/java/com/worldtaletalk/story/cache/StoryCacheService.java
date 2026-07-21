package com.worldtaletalk.story.cache;

import com.worldtaletalk.story.dto.PlayStateCacheDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class StoryCacheService {
    private final RedisTemplate<String, Object> redisTemplate;

    // 키 prefix 상수값
    private static final String CHOICE_STAT_KEY = "story:session:%d:choices";
    private static final String PLAY_STATE_KEY   = "story:session:%d:playState";
    private static final String ENDING_STORY_KEY = "story:session:%d:endingStory";
    private static final String RUNNING_SUMMARY_KEY = "story:session:%d:runningSummary";

    // 레디스 보관 시간
    private static final Duration CACHE_TTL = Duration.ofHours(24); // TODO 일단 24시간 지정 필요하면 수정

    /**
     * 직전 턴 선택지별 스텟 변화량 저장
     *
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
     *
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
     * 플레이어 상태 레디스에 저장
     *
     * @param sessionId 세션 PK
     * @param playStateCacheDTO 상태 정보 DTO
     */
    public void savePlayState(Long sessionId, PlayStateCacheDTO playStateCacheDTO) {
        redisTemplate.opsForValue().set(PLAY_STATE_KEY.formatted(sessionId), playStateCacheDTO, CACHE_TTL);
        log.info("플레이어 상태 캐시 저장 sessionId : {}", sessionId);
    }

    /**
     * 플레이어 상태 캐시값 조회
     *
     * @param sessionId 세션 PK
     * @return 플레이어 상태
     */
    public PlayStateCacheDTO getPlayState(Long sessionId) {
        Object value = redisTemplate.opsForValue().get(PLAY_STATE_KEY.formatted(sessionId));
        return value != null ? (PlayStateCacheDTO) value : null;
    }

    /**
     * 플레이어 상태 캐시 삭제
     *
     * @param sessionId 세션 PK
     */
    public void deletePlayState(Long sessionId) {
        redisTemplate.delete(PLAY_STATE_KEY.formatted(sessionId));
        log.info("플레이어 상태 캐시 삭제 sessionId : {}", sessionId);
    }

    /**
     * 엔딩 스토리 도달시 본문 저장
     *
     * @param sessionId 세션 PK
     */
    public void saveEndingStory(Long sessionId, String endingStory) {
        redisTemplate.opsForValue().set(ENDING_STORY_KEY.formatted(sessionId), endingStory, CACHE_TTL);
        log.info("엔딩 스토리 저장 sessionId : {}", sessionId);
    }

    /**
     * 엔딩 화면 진입시 조회 (없으면 null)
     *
     * @param sessionId 세션 PK
     * @return 엔딩 도달시 저장한 본문
     */
    public String getEndingStory(Long sessionId) {
        Object value = redisTemplate.opsForValue().get(ENDING_STORY_KEY.formatted(sessionId));
        return value != null ? (String) value : null;
    }

    /**
     * 러닝 스토리 요약 레디스 캐시 저장
     *
     * @param sessionId 세션 PK
     * @param runningSummary 러닝 스토리 요약
     */
    public void saveRunningSummary(Long sessionId, String runningSummary) {
        redisTemplate.opsForValue().set(RUNNING_SUMMARY_KEY.formatted(sessionId), runningSummary, CACHE_TTL);
        log.info("러닝 요약 정보 캐시 저장 sessionId : {}", sessionId);
    }

    /**
     * 러닝 스토리 요약 조회
     *
     * @param sessionId 세션 PK
     * @return 러닝 스토리 요약
     */
    public String getRunningSummary(Long sessionId) {
        Object value = redisTemplate.opsForValue().get(RUNNING_SUMMARY_KEY.formatted(sessionId));
        return value != null ? (String) value : null;
    }

    /**
     * 세션 캐시 삭제 (세션 상태가 완결 or 중도 이탈시 제거할때 사용)
     *
     * @param sessionId 세션 PK
     */
    public void deleteSessionCache(Long sessionId) {
        List<String> key = List.of(
                CHOICE_STAT_KEY.formatted(sessionId),
                PLAY_STATE_KEY.formatted(sessionId),
                ENDING_STORY_KEY.formatted(sessionId),
                RUNNING_SUMMARY_KEY.formatted(sessionId)
        );
        redisTemplate.delete(key);
        log.info("세션 캐시 삭제 sessionId : {}", sessionId);
    }

}
