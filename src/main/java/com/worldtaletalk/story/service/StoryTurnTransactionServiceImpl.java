package com.worldtaletalk.story.service;

import com.worldtaletalk.inventory.dto.SessionItemsDTO;
import com.worldtaletalk.inventory.service.SessionItemsService;
import com.worldtaletalk.item.domain.ItemGrade;
import com.worldtaletalk.item.domain.itemgradeprobabilities.ItemGradeProbabilitiesEntity;
import com.worldtaletalk.item.domain.itemgradeprobabilities.PhaseType;
import com.worldtaletalk.item.repository.ItemGradeProbabilitiesRepository;
import com.worldtaletalk.persona.domain.PersonasBaseStatsEntity;
import com.worldtaletalk.persona.repository.PersonasBaseStatsRepository;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoryTurnTransactionServiceImpl implements StoryTurnTransactionService {
    private final WorldsRepository worldsRepository;
    private final ItemGradeProbabilitiesRepository itemGradeProbabilitiesRepository;
    private final StorySessionsRepository storySessionsRepository;
    private final SessionStatsService sessionStatsService;
    private final StoryCacheService storyCacheService;
    private final SessionItemsService sessionItemsService;
    private final StorySessionsMapper storySessionsMapper;
    private final PersonasBaseStatsRepository personasBaseStatsRepository;

    // LLM이 이상한 키값 검증을 위한 페르소나 스텟 상수
    private static final List<String> STAT_KEYS = List.of("wisdom", "strength", "agility", "charisma", "luck");
    // LLM이 이상한 키값 검증을 위한 페르소나 선택지 상수
    private static final List<String> CHOICE_KEYS = List.of("choice1", "choice2", "choice3");

    // 엔딩 판별값 TODO 밸런스 조정 필요시 수정
    private static final int GOOD_THRESHOLD = 20;   // 델타 합산 +20 이상 -> GOOD
    private static final int BAD_THRESHOLD = -20;   // 델타 합산 -20 이하 -> BAD

    /**
     * 턴 시작 처리 (세션 검증, 턴 증가, 선택지 스텟 로그 저장)
     *
     * @param sessionId    세션 PK
     * @param choiceNumber 선택지 (자유입력시 null)
     * @return 선택지 입력시 저장된 스텟 로그 (자유입력시 null)
     */
    @Transactional
    @Override
    public SessionStatLogsDTO beginTurn(Long sessionId, Integer choiceNumber) {
        StorySessionsEntity state = storySessionsRepository.findByState(sessionId).orElseThrow(() ->
                new NoSuchElementException("해당 세션은 존재 하지 않습니다."));

        // 이 메서드에 진입을 했다는건 다음 턴이라는 뜻 (턴수 ++ 처리)
        state.turnUp();

        // 선택지를 선택 했을 경우 레디스에서 스텟변화량 정보 꺼내옴
        Map<String, Integer> appliedStates = null;
        SessionStatLogsDTO appliedStateLog = null;

        if (choiceNumber != null) {
            Map<String, Map<String, Integer>> cached = storyCacheService.getChoiceStats(sessionId);
            if (cached != null) {
                appliedStates = cached.get("choice" + choiceNumber);
                log.info("선택지 입력 choiceNumber : {}, 캐시에서 꺼낸 스텟 : {}", choiceNumber, appliedStates);
            } else {
                log.warn("선택지 입력인데 캐시 없음 sessionId : {}, choiceNumber : {}", sessionId, choiceNumber);
            }
            // 스텟 로그 저장 (LLM 호출 전에 저장해야 합산 스텟에 반영된 상태로 스토리가 생성됨)
            if (appliedStates != null) {
                appliedStateLog = sessionStatsService.addStatLog(buildStatLog(sessionId, state.getCurrentTurn(), appliedStates));
                log.info("beginTurn() - 스텟 로그 저장 완료 turn : {}, 적용 스텟 : {}", state.getCurrentTurn(), appliedStates);
            }
        }
        return appliedStateLog;
    }

    /**
     * 턴 완료 처리 (자유입력 스텟 로그, 아이템, 엔딩, 스냅샷, 레디스 갱신)
     *
     * @param sessionId       세션 PK
     * @param choiceNumber    선택지 (자유입력시 null)
     * @param appliedStateLog beginTurn에서 저장된 스텟 로그 (자유입력시 null)
     * @param llmTurnResult   LLM 응답 파싱 결과
     * @return 턴 처리 결과
     */
    @Transactional
    @Override
    public TurnProcessResult completeTurn(Long sessionId, Integer choiceNumber, SessionStatLogsDTO appliedStateLog, LlmTurnResult llmTurnResult) {
        StorySessionsEntity state = storySessionsRepository.findByState(sessionId).orElseThrow(() ->
                new NoSuchElementException("해당 세션은 존재 하지 않습니다."));

        // 자유 입력시 LLM이 평가한 스텟을 서버 검증후 로그 저장 (응답에서 나오므로 사후 저장)
        if (choiceNumber == null) {
            Map<String, Integer> appliedStates = clampSingleStat(llmTurnResult.getStatChanges());
            log.info("자유 입력 LLM 평가 스텟 검증 결과 : {}", appliedStates);

            appliedStateLog = sessionStatsService.addStatLog(buildStatLog(sessionId, state.getCurrentTurn(), appliedStates));
            log.info("스텟 로그 저장 완료 turn : {}, 적용 스텟 : {}", state.getCurrentTurn(), appliedStates);
        }

        // 스텟, 아이템, 엔딩 도달 처리
        StorySessionsDTO storySessionsDTO = storySessionsMapper.toDTO(state);
        TurnProcessResult turnProcessResult = applyStoryResult(storySessionsDTO, llmTurnResult);
        SessionStatSnapshotsDTO sessionStatSnapshotsDTO = null;

        if (turnProcessResult.getEndingType() != null) {
            state.changeInformationByStorySession(SessionStatus.ENDING);
            state.changeInformationByEndingType(turnProcessResult.getEndingType());
            sessionStatSnapshotsDTO = sessionStatsService.addSnapshot(sessionId);
            log.info("엔딩 도달 sessionId : {}, endingType : {}", sessionId, turnProcessResult.getEndingType());
            storyCacheService.deleteSessionCache(sessionId);
        } else { // 이번 응답으로 선택지별 다음 턴용으로 레디스에 스텟 저장
            storyCacheService.saveChoiceStats(sessionId, clampStatChanges(llmTurnResult.getStatChangesByChoice()));
        }

        StorySessionsEntity save = storySessionsRepository.save(state);

        // 최신값이 갱신 되기전에 DTO변환이 될 수 있으니 확실하게 변환후 저장
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

    /**
     * 보상 처리 (LLM 요청 실패시 턴 증가와 스텟 로그 원상 복구)
     *
     * @param sessionId       세션 PK
     * @param appliedStateLog beginTurn에서 저장된 스텟 로그 (없으면 null)
     */
    @Transactional
    @Override
    public void compensateTurn(Long sessionId, SessionStatLogsDTO appliedStateLog) {
        StorySessionsEntity state = storySessionsRepository.findById(sessionId).orElseThrow(() ->
                new NoSuchElementException("해당 세션은 존재 하지 않습니다."));

        // 증가했던 턴 원상 복구
        state.turnDown();

        // 선택지 입력으로 저장됐던 스텟 로그 삭제
        if (appliedStateLog != null) {
            sessionStatsService.deleteStatLog(appliedStateLog.getId());
        }
        log.warn("LLM 호출 실패 보상 처리 완료 sessionId : {}", sessionId);
    }

    /* Helper Methods */

    /**
     * 스텟 로그를 DTO로 반환 하기 위한 메서드
     *
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
     *
     * @param rawStat 검증이 필요한 스텟 Map값
     * @return 보정된 스텟 값
     */
    private Map<String, Integer> clampSingleStat(Map<String, Integer> rawStat) {
        Map<String, Integer> clamped = new HashMap<>();
        for (String key : STAT_KEYS) {
            // 맵 자체가 null 이거나 키가 없거나 값이 명시적 null 인 경우 모두 0 처리
            Integer raw = rawStat != null ? rawStat.get(key) : null;
            int value = raw != null ? raw : 0;
            // LLM이 범위 초과 일시 -2 ~ 2 로 범위 지정
            clamped.put(key, Math.clamp(value, -2, 2));
        }
        return clamped;
    }

    /**
     * 선택지별 스텟 범위 제한
     *
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
     *
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
     *
     * @param itemContext LLM 아이템 힌트
     * @return 매핑할 아이템 등급
     */
    private ItemGrade contextToGrade(ItemContext itemContext) {
        return switch (itemContext) {
            case MINOR -> ItemGrade.COMMON;
            case NORMAL -> ItemGrade.RARE;
            case SIGNIFICANT -> ItemGrade.EPIC;
            case LEGENDARY -> ItemGrade.CURSED; // TODO 현재 레전더리 등급은 존재하지 않음 추가할까?
        };
    }

    /**
     * 아이템 등급을 결정할 헬퍼 메서드
     *
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

        // 사실 크게 의미는 없어보이긴한데 그냥 순서보장 하기 위해서 HashMap -> LinkedHashMap으로 변경
        Map<ItemGrade, Integer> weightMap = new LinkedHashMap<>();
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
     * 엔딩 타입 판별 (스탯 합산 + 아이템 보너스 까지 포함)
     * SECRET -> DARK -> GOOD -> BAD -> TRUE 로 판별함 이유 : 히든과 흑화 엔딩조건 둘다 해당할 경우 충돌 가능성이 있기 때문
     *
     * @param sessionId  세션 PK
     * @param personaId  페르소나 PK (기본 스탯 조회용)
     * @param endingFlag LLM 엔딩 트리거 여부
     * @param hiddenEventFlag LLM 히든 엔딩 트리거 여부
     * @return 결정된 엔딩 타입 (endingFlag=false면 null)
     */
    private EndingType resolveEndingType(Long sessionId, Long personaId, boolean endingFlag, boolean hiddenEventFlag) {
        if (hiddenEventFlag) return EndingType.SECRET; // 히든은 LLM이 이벤트 발생여부만 확인하고 엔딩을 결정짓기때문에 밑에 로직은 필요없어서 제일 위로 올림
        if (!endingFlag) return null;

        // 페르소나 기본 스탯
        PersonasBaseStatsEntity baseStats = personasBaseStatsRepository.findByPersonaId(personaId).orElseThrow(() ->
                new NoSuchElementException("해당 페르소나의 기본 스탯이 존재하지 않습니다."));

        // 현재 스탯
        Map<String, Integer> currentStats = sessionStatsService.getCurrentStats(sessionId);

        // 보유 중인 인벤토리
        List<SessionItemsDTO> inventory = sessionItemsService.getInventory(sessionId);

        // 아이템 보너스 합산 -> 최종 스탯 계산
        Map<String, Integer> itemBonuses = SessionItemsDTO.sumBonuses(inventory);
        int finalWisdom   = currentStats.get("wisdom")   + itemBonuses.get("wisdom");
        int finalStrength = currentStats.get("strength") + itemBonuses.get("strength");
        int finalAgility  = currentStats.get("agility")  + itemBonuses.get("agility");
        int finalCharisma = currentStats.get("charisma") + itemBonuses.get("charisma");
        int finalLuck     = currentStats.get("luck")     + itemBonuses.get("luck");

        // 기본 스탯 합산
        int baseTotal = baseStats.getWisdom() + baseStats.getStrength() + baseStats.getAgility()
                      + baseStats.getCharisma() + baseStats.getLuck();

        // 최종 스탯 합산 및 delta 계산
        int finalTotal = finalWisdom + finalStrength + finalAgility + finalCharisma + finalLuck;
        int deltaSum = finalTotal - baseTotal;

        log.info("엔딩 판별 시작 sessionId: {}, baseTotal: {}, finalTotal: {}, deltaSum: {}",
                sessionId, baseTotal, finalTotal, deltaSum);

        // DARK : 지혜 + 카리스마 최종값이 기본값 합산 이하 (음수)
        int baseWcSum   = baseStats.getWisdom() + baseStats.getCharisma();
        int finalWcSum  = finalWisdom + finalCharisma;
        if (finalWcSum <= baseWcSum) {
            log.info("엔딩 판별 결과 DARK (지혜+카리스마 delta: {})", finalWcSum - baseWcSum);
            return EndingType.DARK;
        }

        // GOOD : 전체 delta 합산 기준치 이상
        if (deltaSum >= GOOD_THRESHOLD) {
            log.info("엔딩 판별 결과 GOOD (deltaSum: {})", deltaSum);
            return EndingType.GOOD;
        }

        // BAD : 전체 delta 합산 기준치 이하
        if (deltaSum <= BAD_THRESHOLD) {
            log.info("엔딩 판별 결과 BAD (deltaSum: {})", deltaSum);
            return EndingType.BAD;
        }

        // TRUE : 어떤 조건도 해당 없는 기본 엔딩
        log.info("엔딩 판별 결과 TRUE (deltaSum: {})", deltaSum);
        return EndingType.TRUE;
    }

    /**
     * 아이템 이벤트 처리 + 엔딩 판별
     *
     * @param storySessionsDTO 세션 정보
     * @param llmTurnResult    LLM 응답 파싱 결과
     * @return 턴 처리 결과
     */
    private TurnProcessResult applyStoryResult(StorySessionsDTO storySessionsDTO, LlmTurnResult llmTurnResult) {
        // 아이템 처리 로직
        SessionItemsDTO sessionItemsDTO = null;
        Long removeItemId = null;

        if (llmTurnResult.getItemEvent() != null && llmTurnResult.getItemEvent().getType() != ItemEventType.NULL) {
            ItemGrade itemGrade = resolveItemGrade(llmTurnResult.getItemEvent().getItemContext(), storySessionsDTO);
            log.info("아이템 이벤트 type : {}, 힌트 : {}, 확정 등급 : {}",
                    llmTurnResult.getItemEvent().getType(), llmTurnResult.getItemEvent().getItemContext(), itemGrade);

            if (llmTurnResult.getItemEvent().getType() == ItemEventType.GAIN) {
                // 아이템 생성 (등급 설정 기반 보정치 랜덤 생성)
                sessionItemsDTO = sessionItemsService.addItem(
                        storySessionsDTO.getId(),
                        storySessionsDTO.getCurrentTurn(),
                        llmTurnResult.getItemEvent().getItemName(),
                        llmTurnResult.getItemEvent().getItemDescription(),
                        itemGrade
                );
                log.info("아이템 획득 itemName : {}, grade : {}", sessionItemsDTO.getItemName(), sessionItemsDTO.getGrade());

            } else if (llmTurnResult.getItemEvent().getType() == ItemEventType.LOSE) {
                // 아이템 버림 (이름 매칭, LLM 환각으로 없는 아이템일 수 있어서 null 허용)
                removeItemId = sessionItemsService.dropItemByName(
                        storySessionsDTO.getId(),
                        llmTurnResult.getItemEvent().getItemName()
                );
                if (removeItemId != null) {
                    log.info("아이템 소실 itemId : {}, itemName : {}", removeItemId, llmTurnResult.getItemEvent().getItemName());
                } else {
                    log.warn("아이템 소실 실패 (보유하지 않은 아이템) itemName : {}", llmTurnResult.getItemEvent().getItemName());
                }
            }
        }

        // 엔딩 타입 판별 (아이템 처리 완료 후 - 이번 턴 획득 아이템도 반영됨)
        EndingType endingType = resolveEndingType(
                storySessionsDTO.getId(),
                storySessionsDTO.getPersonaId(),
                llmTurnResult.isEndingFlag(),
                llmTurnResult.isHiddenEventFlag()
        );

        return TurnProcessResult.builder()
                .session(storySessionsDTO)
                .createdItem(sessionItemsDTO)
                .removedItemId(removeItemId)
                .endingType(endingType)
                .build();
    }
}
