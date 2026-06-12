package com.worldtaletalk.inventory.service;

import com.worldtaletalk.inventory.SessionItemsMapper;
import com.worldtaletalk.inventory.domain.SessionItemsEntity;
import com.worldtaletalk.inventory.dto.SessionItemsDTO;
import com.worldtaletalk.inventory.repository.SessionItemsRepository;
import com.worldtaletalk.item.domain.ItemGrade;
import com.worldtaletalk.session.domain.StorySessionsEntity;
import com.worldtaletalk.session.repository.StorySessionsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Log4j2
@Service
@RequiredArgsConstructor
public class SessionItemsServiceImpl implements SessionItemsService {
    private final SessionItemsRepository sessionItemsRepository;
    private final StorySessionsRepository storySessionsRepository;
    private final SessionItemsMapper sessionItemsMapper;

    /**
     * 아이템 생성 (등급 설정 기반 보정치 랜덤 생성)
     * @param sessionId       세션 PK
     * @param turn            획득 턴
     * @param itemName        LLM 생성 아이템 이름
     * @param itemDescription LLM 생성 아이템 설명
     * @param grade           서버 확정 등급
     * @return 생성된 아이템
     */
    @Override
    public SessionItemsDTO addItem(Long sessionId, int turn, String itemName, String itemDescription, ItemGrade grade) {
        return null;
    }

    /**
     * 아이템 버림 처리 (이름 매칭, 없으면 null)
     * @param sessionId 세션 PK
     * @param itemName  버릴 아이템 이름
     * @param turn      버린 턴
     * @return 버려진 아이템 PK (보유 중이 아니면 null)
     */
    @Override
    public Long dropItem(Long sessionId, String itemName, int turn) {
        StorySessionsEntity storySession = storySessionsRepository.findById(sessionId).orElseThrow(() ->
                new NoSuchElementException("해당 세션은 존재하지 않습니다."));
        SessionItemsEntity sessionItems = sessionItemsRepository.findAllByItemNameAndActiveTrue(storySession.getId(), itemName)
                .orElse(null);

        // 아이템 버림 처리
        if (sessionItems != null) {
            sessionItems.dropItem(turn);
            return sessionItems.getId();
        }
        return null;
    }

    /**
     * 보유 중 인벤토리 조회
     * @param sessionId 세션 PK
     * @return 보유 아이템 목록
     */
    @Override
    public List<SessionItemsDTO> getInventory(Long sessionId) {
        StorySessionsEntity storySession = storySessionsRepository.findById(sessionId).orElseThrow(() ->
                new NoSuchElementException("해당 세션은 존재하지 않습니다."));
        List<SessionItemsEntity> sessionItems = sessionItemsRepository.findAllByItemActive(storySession.getId());
        return sessionItemsMapper.toDTOList(sessionItems);
    }
}
