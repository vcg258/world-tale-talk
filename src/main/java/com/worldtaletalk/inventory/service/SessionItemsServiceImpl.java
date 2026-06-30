package com.worldtaletalk.inventory.service;

import com.worldtaletalk.inventory.SessionItemsMapper;
import com.worldtaletalk.inventory.domain.SessionItemsEntity;
import com.worldtaletalk.inventory.dto.SessionItemsDTO;
import com.worldtaletalk.inventory.repository.SessionItemsRepository;
import com.worldtaletalk.item.mapper.ItemGradeConfigsMapper;
import com.worldtaletalk.item.domain.ItemGrade;
import com.worldtaletalk.item.domain.ItemGradeConfigsEntity;
import com.worldtaletalk.item.dto.ItemGradeConfigsDTO;
import com.worldtaletalk.item.repository.ItemGradeConfigsRepository;
import com.worldtaletalk.session.domain.StorySessionsEntity;
import com.worldtaletalk.session.repository.StorySessionsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionItemsServiceImpl implements SessionItemsService {
    private final SessionItemsRepository sessionItemsRepository;
    private final StorySessionsRepository storySessionsRepository;
    private final SessionItemsMapper sessionItemsMapper;
    private final ItemGradeConfigsRepository itemGradeConfigsRepository;
    private final ItemGradeConfigsMapper itemGradeConfigsMapper;

    /**
     * 아이템 생성 (등급 설정 기반 보정치 랜덤 생성)
     *
     * @param sessionId       세션 PK
     * @param turn            획득 턴
     * @param itemName        LLM 생성 아이템 이름
     * @param itemDescription LLM 생성 아이템 설명
     * @param grade           서버 확정 등급
     * @return 생성된 아이템
     */
    @Transactional
    @Override
    public SessionItemsDTO addItem(Long sessionId, int turn, String itemName, String itemDescription, ItemGrade grade) {
        StorySessionsEntity storySession = storySessionsRepository.findById(sessionId).orElseThrow(() ->
                new NoSuchElementException("해당 세션은 존재하지 않습니다."));

        // 해당 등급의 보정치 설정 조회 (statSumMax, singleMin, singleMax, canNegative)
        ItemGradeConfigsEntity gradeConfigs = itemGradeConfigsRepository.findByGrade(grade).orElseThrow(() ->
                new NoSuchElementException("해당 등급 보정치 설정이 존재하지 않습니다: " + grade));
        ItemGradeConfigsDTO dto = itemGradeConfigsMapper.toDTO(gradeConfigs);
        log.info("현재 {}등급 보정치 : {}", dto.getGrade(), dto);

        // 등급 설정 기반 스텟 보정치 랜덤 생성
        Map<String, Integer> bonusStat = generateBonuses(dto);

        // 아이템 생성
        SessionItemsEntity item = SessionItemsEntity.createItem(
                itemName,
                itemDescription,
                grade,
                bonusStat.get("wisdom"),
                bonusStat.get("strength"),
                bonusStat.get("agility"),
                bonusStat.get("charisma"),
                bonusStat.get("luck"),
                turn,
                storySession
        );

        SessionItemsEntity save = sessionItemsRepository.save(item);
        log.info("아이템 생성 : {}", save);

        return sessionItemsMapper.toDTO(save);
    }

    /**
     * 아이템 버림 처리 (유저가 직접 버림용)
     *
     * @param sessionId 세션 PK
     * @param itemId    버릴 아이템 PK
     */
    @Transactional
    @Override
    public void dropItem(Long sessionId, Long itemId) {
        StorySessionsEntity storySession = storySessionsRepository.findById(sessionId).orElseThrow(() ->
                new NoSuchElementException("해당 세션은 존재하지 않습니다."));
        SessionItemsEntity item = sessionItemsRepository.findById(itemId).orElseThrow(() ->
                new NoSuchElementException("해당 아이템은 존재하지 않습니다."));

        if (!item.getStorySession().getId().equals(sessionId)) {
            throw new IllegalArgumentException("해당 세션의 아이템이 아닙니다");
        }

        item.dropItem(storySession.getCurrentTurn());
        SessionItemsEntity save = sessionItemsRepository.save(item);
        log.info("아이템 버림 처리 완료 : {}", save);
    }

    /**
     * 아이템 이름으로 버림 처리 (LLM)
     *
     * @param sessionId 세션 PK
     * @param itemName  버릴 아이템 이름
     * @return 버린 아이템 PK (보유하지 않은 아이템이면 null)
     */
    @Transactional
    @Override
    public Long dropItemByName(Long sessionId, String itemName) {
        StorySessionsEntity storySession = storySessionsRepository.findById(sessionId).orElseThrow(() ->
                new NoSuchElementException("해당 세션은 존재하지 않습니다."));
        SessionItemsEntity item = sessionItemsRepository.findAllByItemNameAndActiveTrue(sessionId, itemName)
                .orElse(null);
        if (item == null) {
            log.warn("아이템 소실 실패 보유하지 않음 itemName : {}, sessionId : {}", itemName, sessionId);
            return null;
        }
        item.dropItem(storySession.getCurrentTurn());
        SessionItemsEntity save = sessionItemsRepository.save(item);
        log.info("아이템 버림 처리 완료 : {}", save);
        return save.getId();
    }

    /**
     * 보유 중 인벤토리 조회
     *
     * @param sessionId 세션 PK
     * @return 보유 아이템 목록
     */
    @Transactional(readOnly = true)
    @Override
    public List<SessionItemsDTO> getInventory(Long sessionId) {
        if (!storySessionsRepository.existsById(sessionId)) throw new NoSuchElementException("해당 세션은 존재 하지 않습니다.");
        List<SessionItemsEntity> sessionItems = sessionItemsRepository.findAllByItemActive(sessionId);
        return sessionItemsMapper.toDTOList(sessionItems);
    }

    /* Helper Methods */

    /**
     * 등급 설정 기반 스텟 보정치 랜덤 생성
     *
     * @param itemGradeConfigsDTO 등급 보정치 설정 정보
     * @return 스텟 보정치 Map
     */
    private Map<String, Integer> generateBonuses(ItemGradeConfigsDTO itemGradeConfigsDTO) {
        List<String> statKeys = List.of("wisdom", "strength", "agility", "charisma", "luck");
        Random random = new Random();
        Map<String, Integer> bonuses = new HashMap<>();

        // 전체 보정치 예산 (스텟 절대값 합이 이 값을 초과할 수 없음)
        int budget = itemGradeConfigsDTO.getStatSumMax();

        for (String key : statKeys) {
            // 예산 소진 시 나머지 스텟은 0 처리
            if (budget <= 0) {
                bonuses.put(key, 0);
                continue;
            }

            // 단일 스텟 최대치와 남은 예산 중 작은 값으로 상한 결정
            int max = Math.min(itemGradeConfigsDTO.getSingleMax(), budget);

            int value;
            if (itemGradeConfigsDTO.getCanNegative() && random.nextBoolean()) {
                // 음수 허용 등급 (CURSED 같은 저주 아이템) - singleMin 기준으로 음수 생성
                value = -random.nextInt(Math.abs(itemGradeConfigsDTO.getSingleMin()) + 1);
            } else {
                // 0 이상 max 이하의 양수 보정치 생성
                value = random.nextInt(max + 1);
            }

            bonuses.put(key, value);
            // 절대값 기준으로 예산 차감 (양수든 음수든 예산 소모)
            budget -= Math.abs(value);
        }
        return bonuses;
    }
}
