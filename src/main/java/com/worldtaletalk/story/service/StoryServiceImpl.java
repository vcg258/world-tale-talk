package com.worldtaletalk.story.service;

import com.worldtaletalk.inventory.dto.SessionItemsDTO;
import com.worldtaletalk.item.domain.ItemGrade;
import com.worldtaletalk.item.domain.itemgradeprobabilities.ItemGradeProbabilitiesEntity;
import com.worldtaletalk.item.domain.itemgradeprobabilities.PhaseType;
import com.worldtaletalk.item.repository.ItemGradeProbabilitiesRepository;
import com.worldtaletalk.session.StorySessionsMapper;
import com.worldtaletalk.session.domain.EndingType;
import com.worldtaletalk.session.domain.SessionStatus;
import com.worldtaletalk.session.domain.StorySessionsEntity;
import com.worldtaletalk.session.dto.StorySessionsDTO;
import com.worldtaletalk.session.repository.StorySessionsRepository;
import com.worldtaletalk.stat.dto.SessionStatLogsDTO;
import com.worldtaletalk.stat.dto.SessionStatSnapshotsDTO;
import com.worldtaletalk.stat.service.SessionStatsService;
import com.worldtaletalk.story.cache.StoryCacheService;
import com.worldtaletalk.story.dto.ItemContext;
import com.worldtaletalk.story.dto.ItemEventType;
import com.worldtaletalk.story.dto.LlmTurnResult;
import com.worldtaletalk.story.dto.TurnProcessResult;
import com.worldtaletalk.world.domain.WorldsEntity;
import com.worldtaletalk.world.repository.WorldsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Log4j2
@Service
@RequiredArgsConstructor
public class StoryServiceImpl implements StoryService {
    private final StorySessionsRepository storySessionsRepository;
    private final WorldsRepository worldsRepository;
    private final ItemGradeProbabilitiesRepository itemGradeProbabilitiesRepository;
    private final StoryAiService storyAiService;
    private final StorySessionsMapper storySessionsMapper;
    private final SessionStatsService sessionStatsService;
    private final StoryCacheService storyCacheService;

    // LLM이 이상한 키값 검증을 위한 페르소나 스텟 상수
    private static final List<String> STAT_KEYS = List.of("wisdom", "strength", "agility", "charisma", "luck");
    // LLM이 이상한 키값 검증을 위한 페르소나 선택지 상수
    private static final List<String> CHOICE_KEYS = List.of("choice1", "choice2", "choice3");


    /**
     * LLM 호출후 스텟 / 아이템 / 엔딩 / 턴수 처리
     * @param sessionId 세션 PK
     * @param userInput 유저 프롬프트
     * @return 로직 처리된 세션 정보
     */
    @Transactional
    @Override
    public TurnProcessResult processStory(Long sessionId, String userInput, Integer choiceNumber) {
        StorySessionsEntity state = storySessionsRepository.findByState(sessionId).orElseThrow(() ->
                new NoSuchElementException("해당 세션은 존재 하지 않습니다."));

        // 선택지를 선택 했을 경우 레디스에서 스텟변화량 정보 꺼내옴
        Map<String, Integer> appliedStates = null;
        if (choiceNumber != null) {
            Map<String, Map<String, Integer>> cached = storyCacheService.getChoiceStats(sessionId);
            if (cached != null) {
                appliedStates = cached.get("choice" + choiceNumber);
                log.info("선택지 입력 choiceNumber : {}, 캐시에서 꺼낸 스텟 : {}", choiceNumber, appliedStates);
            } else {
                log.warn("선택지 입력인데 캐시 없음 sessionId : {}, choiceNumber : {}", sessionId, choiceNumber);
            }
        }

        // LLM 호출
        LlmTurnResult llmTurnResult = storyAiService.generateStory(sessionId, userInput);

        // 자유 입력시 선택지는 null
        if (choiceNumber == null) {
            // 자유 입력시 서버 검증후 사용
            appliedStates = clampSingleStat(llmTurnResult.getStatChanges());
            log.info("자유 입력 LLM 평가 스텟 검증 결과 : {}", appliedStates);
        }

        // 스텟 로그 저장
        SessionStatLogsDTO appliedStateLog = null;
        if (appliedStates != null) {
            appliedStateLog = sessionStatsService.addStatLog(buildStatLog(sessionId, state.getCurrentTurn(), appliedStates));
            log.info("스텟 로그 저장 완료 turn : {}, 적용 스텟 : {}", state.getCurrentTurn(), appliedStates);
        }

        // 스텟, 아이템 처리
        StorySessionsDTO storySessionsDTO = storySessionsMapper.toDTO(state);
        TurnProcessResult turnProcessResult = applyStoryResult(storySessionsDTO, llmTurnResult);

        // 엔딩
        SessionStatSnapshotsDTO sessionStatSnapshotsDTO = null;
        if (turnProcessResult.getEndingType() != null) {
            state.changeInformationByStorySession(SessionStatus.ENDING);
            state.changeInformationByEndingType(turnProcessResult.getEndingType());
            sessionStatSnapshotsDTO = sessionStatsService.addSnapshot(sessionId);
            log.info("엔딩 도달 sessionId : {}, endingType : {}", sessionId, turnProcessResult.getEndingType());
        }
        // 턴수 ++ 처리
        state.turnUp();

        // 이번 응답으로 선택지별 다음 턴용으로 레디스에 스텟 저장
        storyCacheService.saveChoiceStats(sessionId, clampStatChanges(llmTurnResult.getStatChangesByChoice()));

        // 엔딩 도달 레디스 캐시 삭제
        if (turnProcessResult.getEndingType() != null) {
            storyCacheService.deleteSessionCache(sessionId);
        }

        StorySessionsEntity save = storySessionsRepository.save(state);

        // 최신값이 갱신 되기전에 RDB에 저장 될 수 있으니 확실하게 저장
        StorySessionsDTO latestSession = storySessionsMapper.toDTO(save);

        log.info("턴 처리 완료 sessionId : {}, currentTurn : {}", sessionId, state.getCurrentTurn());

        return TurnProcessResult.builder()
                .session(latestSession)
                .createdItem(turnProcessResult.getCreatedItem())
                .removedItemId(turnProcessResult.getRemovedItemId())
                .endingType(turnProcessResult.getEndingType())
                .snapshot(sessionStatSnapshotsDTO)
                .appliedStatLog(appliedStateLog)
                .build();
    }

    /* Helper Methode */

    /**
     * 스텟 로그를 DTO로 반환 하기 위한 메서드
     * @param sessionId 세션 PK
     * @param turn 현재 턴
     * @param stat 검증된 스탯
     * @return 스탯 로그
     */
    private SessionStatLogsDTO buildStatLog(Long sessionId, int turn, Map<String, Integer> stat) {
        return SessionStatLogsDTO.builder()
                .storySessionId(sessionId)
                .turn(turn)
                .wisdomDelta(stat.getOrDefault("wisdom", 0))
                .strengthDelta(stat.getOrDefault("strength", 0))
                .agilityDelta(stat.getOrDefault("agility", 0))
                .charismaDelta(stat.getOrDefault("charisma", 0))
                .luckDelta(stat.getOrDefault("luck", 0))
                .build();
    }

    /**
     * 스텟 +2 or -2 범위 제한
     * @param rawStat 검증이 필요한 스텟 Map값
     * @return 보정된 스텟 값
     */
    private Map<String, Integer> clampSingleStat(Map<String, Integer> rawStat) {
        Map<String, Integer> clamped = new HashMap<>();
        for (String key : STAT_KEYS) {
            // 벨류값이 null이 더라도 0으로 디폴트 값 지정 : 만약 키값이 없다 0 확정
            Integer value = rawStat != null ? rawStat.getOrDefault(key, 0) : 0;
            // LLM이 범위 초과 일시 -2 ~ 2 로 범위 지정
            clamped.put(key, Math.clamp(value, -2, 2));
        }
        return clamped;
    }

    /**
     * 선택지별 스텟 범위 제한
     * @param rawChanges 검증이 필요한 선택지 Map
     * @return 보정된 선택지 값
     */
    private Map<String, Map<String, Integer>> clampStatChanges(Map<String, Map<String, Integer>> rawChanges) {
        Map<String, Map<String, Integer>> clamped = new HashMap<>();
        for (String key : CHOICE_KEYS) {
            Map<String, Integer> rawStat = rawChanges != null ? rawChanges.get(key) : null;
            clamped.put(key, clampSingleStat(rawStat));
        }
        return clamped;
    }

    /**
     * 아이템 확률을 정하기 위한 진행도 퍼센트 계산
     * @param storySessionsDTO 세션 정보
     * @return 진행도
     */
    private PhaseType resolvePhase(StorySessionsDTO storySessionsDTO) {
        WorldsEntity world = worldsRepository.findById(storySessionsDTO.getWorldId()).orElseThrow(() ->
                new NoSuchElementException("해당 세계관은 존재하지 않습니다."));
        double ratio = (double) storySessionsDTO.getCurrentTurn() / world.getTargetTurn();
        if (ratio < 0.33) return PhaseType.EARLY; // 초입
        if (ratio < 0.66) return PhaseType.MID; // 중간
        return PhaseType.LATE; // 완결
    }

    /**
     * LLM이 등급 힌트를 제공할때 매핑 할 메서드
     * @param itemContext LLM 아이템 힌트
     * @return 매핑할 아이템 등급
     */
    private ItemGrade contextToGrade(ItemContext itemContext) {
        return switch (itemContext) {
            case MINOR -> ItemGrade.COMMON;
            case NORMAL -> ItemGrade.RARE;
            case SIGNIFICANT -> ItemGrade.EPIC;
            case LEGENDARY -> ItemGrade.CURSED;
        };
    }

    /**
     * 아이템 등급을 결정할 헬퍼 메서드
     * @param itemContext LLM 아이템 힌트
     * @param storySessionsDTO 세션 정보
     * @return 아이템 등급 확정
     */
    private ItemGrade resolveItemGrade(ItemContext itemContext, StorySessionsDTO storySessionsDTO) {
        PhaseType phaseType = resolvePhase(storySessionsDTO);
        List<ItemGradeProbabilitiesEntity> probabilities = itemGradeProbabilitiesRepository.findAllByPhase(phaseType);
        // Phase로 조회를 했는데 비었을 경우 COMMON 으로 fallback
        if (probabilities.isEmpty()) {
            return ItemGrade.COMMON;
        }
        ItemGrade boostedGrade = contextToGrade(itemContext);

        Map<ItemGrade, Integer> weightMap = new HashMap<>();
        for (ItemGradeProbabilitiesEntity entity : probabilities) {
            // TODO 아이템힌트가 떳을시 확률 2배 필요하면 수정
            int weight = entity.getGrade() == boostedGrade ? entity.getProbability() * 2 : entity.getProbability();
            weightMap.put(entity.getGrade(), weight);
        }

        // 랜덤 확률
        // 가중치를 모두 더함
        int total = weightMap.values().stream().mapToInt(Integer::intValue).sum();
        // 만약 가중치가 0 일경우 COMMON fallback
        if (total == 0) {
            return ItemGrade.COMMON;
        }
        // 모두 더한 가중치에서 랜덤으로 돌림
        int random = new Random().nextInt(total);
        // 뽑힌 랜덤 가중치 값에서 무슨 등급 아이템인지 순회하여 조건문에 일치하면 확정
        int cumulative = 0;
        for (Map.Entry<ItemGrade, Integer> entry : weightMap.entrySet()) {
            cumulative += entry.getValue();
            if (random < cumulative) return entry.getKey();
        }
        return ItemGrade.COMMON;
    }

    /**
     * 엔딩 결정 메서드
     * @param storySessionsDTO 세션 정보
     * @param llmTurnResult LLM이 임의로 할당한 스텟, 아이템, 엔딩, 턴 정보
     * @return 엔딩 결정
     */
    private EndingType resolveEndingType(StorySessionsDTO storySessionsDTO, LlmTurnResult llmTurnResult) {
        // 엔딩 조건 미충족
        if (!llmTurnResult.isEndingFlag()) {
            return null;
        }

        return EndingType.TRUE; // TODO 일단 기본 엔딩만 다른건 스텟로직 구현후 다른 엔딩 추가

    }

    /**
     * 아이템, 엔딩 검증 처리
     * @param storySessionsDTO 세션정보
     * @param llmTurnResult LLM이 임의로 할당한 스텟, 아이템, 엔딩, 턴 정보
     * @return 검증된 정보
     */
    private TurnProcessResult applyStoryResult(StorySessionsDTO storySessionsDTO, LlmTurnResult llmTurnResult) {
        // 아이템 처리 로직
        SessionItemsDTO sessionItemsDTO = null;
        Long removeItemId = null;

        if (llmTurnResult.getItemEvent() != null && llmTurnResult.getItemEvent().getType() != ItemEventType.NULL) {
            ItemGrade itemGrade = resolveItemGrade(llmTurnResult.getItemEvent().getItemContext(),  storySessionsDTO);
            log.info("아이템 이벤트 type : {}, 힌트 : {}, 확정 등급 : {}",
                    llmTurnResult.getItemEvent().getType(), llmTurnResult.getItemEvent().getItemContext(), itemGrade);

            if (llmTurnResult.getItemEvent().getType() == ItemEventType.GAIN) {
                // TODO 인벤토리 서비스 미구현 구현후 생성로직 추가
            } else if (llmTurnResult.getItemEvent().getType() == ItemEventType.LOSE) {
                // TODO 인벤토리 서비스 미구현 구현후 버림로직 추가
            }
        }

        // 엔딩 타입 판별
        EndingType endingType = resolveEndingType(storySessionsDTO, llmTurnResult);


        return TurnProcessResult.builder()
                .session(storySessionsDTO)
                .createdItem(sessionItemsDTO)
                .removedItemId(removeItemId)
                .endingType(endingType)
                .build();
    }
}
