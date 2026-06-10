package com.worldtaletalk.story.service;

import com.worldtaletalk.inventory.dto.SessionItemsDTO;
import com.worldtaletalk.item.domain.ItemGrade;
import com.worldtaletalk.item.domain.itemgradeprobabilities.ItemGradeProbabilitiesEntity;
import com.worldtaletalk.item.domain.itemgradeprobabilities.PhaseType;
import com.worldtaletalk.item.repository.ItemGradeProbabilitiesRepository;
import com.worldtaletalk.session.domain.EndingType;
import com.worldtaletalk.session.domain.StorySessionsEntity;
import com.worldtaletalk.session.dto.StorySessionsDTO;
import com.worldtaletalk.session.repository.StorySessionsRepository;
import com.worldtaletalk.stat.dto.SessionStatLogsDTO;
import com.worldtaletalk.story.dto.ItemContext;
import com.worldtaletalk.story.dto.LlmTurnResult;
import com.worldtaletalk.story.dto.TurnProcessResult;
import com.worldtaletalk.world.domain.WorldsEntity;
import com.worldtaletalk.world.repository.WorldsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.*;

@Log4j2
@Service
@RequiredArgsConstructor
public class StoryServiceImpl implements StoryService {
    private final StorySessionsRepository storySessionsRepository;
    private final WorldsRepository worldsRepository;
    private final ItemGradeProbabilitiesRepository itemGradeProbabilitiesRepository;


    @Override
    public TurnProcessResult processStory(Long sessionId, String userInput) {
        StorySessionsEntity state = storySessionsRepository.findByState(sessionId).orElseThrow(() ->
                new NoSuchElementException("해당 세션은 존재 하지 않습니다."));

        return null;
    }

    /* Helper Methode */

    /**
     * 스텟 +2 or -2 범위 제한
     * @param rawChanges 검증이 필요한 스텟 Map값
     * @return 보정된 스텟 값
     */
    private Map<String, Integer> clampStatChanges(Map<String, Integer> rawChanges) {
        Map<String, Integer> clamped = new HashMap<>();
        for (Map.Entry<String, Integer> entry : rawChanges.entrySet()) {
            int value = Math.clamp(entry.getValue(), -2, 2);
            clamped.put(entry.getKey(), value);
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

        ItemGrade boostedGrade = contextToGrade(itemContext);

        Map<ItemGrade, Integer> weightMap = new HashMap<>();
        for (ItemGradeProbabilitiesEntity entity : probabilities) {
            // TODO 아이템힌트가 떳을시 확률 2배 필요하면 수정
            int weight = entity.getGrade() == boostedGrade ? entity.getProbability() * 2 : entity.getProbability();
            weightMap.put(boostedGrade, weight);
        }

        // 랜덤 확률
        // 가중치를 모두 더함
        int total = weightMap.values().stream().mapToInt(Integer::intValue).sum();
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

    private EndingType resolveEndingType(StorySessionsDTO storySessionsDTO, LlmTurnResult llmTurnResult) {
        // 엔딩 조건 미충족
        if (!llmTurnResult.isEndingFlag()) {
            return null;
        }

        return EndingType.TRUE; // TODO 일단 기본 엔딩만 다른건 스텟로직 구현후 추가

    }

    /**
     * 스텟, 아이템, 엔딩 검증 처리
     * @param storySessionsDTO 세션정보
     * @param llmTurnResult LLM이 임의로 할당한 스텟, 아이템, 엔딩, 턴 정보
     * @return 검증된 정보
     */
    private TurnProcessResult applyStoryResult(StorySessionsDTO storySessionsDTO, LlmTurnResult llmTurnResult) {
        // 스탯 변화 범위 제한 지정
        Map<String, Integer> clamped = clampStatChanges(llmTurnResult.getStatChanges());

        SessionStatLogsDTO statLogsDTO = null; // TODO 스텟 로그 생성 아직 미구현이기때문에 구현후 넣기

        // 아이템 생성
        SessionItemsDTO sessionItemsDTO = null;
        Long removeItemId = null;

        if (llmTurnResult.getItemEvent() != null) {}
        return null;
    }
}
