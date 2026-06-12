package com.worldtaletalk.inventory.service;

import com.worldtaletalk.inventory.dto.SessionItemsDTO;
import com.worldtaletalk.item.domain.ItemGrade;

import java.util.List;

public interface SessionItemsService {
    /**
     * 아이템 생성 (등급 설정 기반 보정치 랜덤 생성)
     * @param sessionId       세션 PK
     * @param turn            획득 턴
     * @param itemName        LLM 생성 아이템 이름
     * @param itemDescription LLM 생성 아이템 설명
     * @param grade           서버 확정 등급
     * @return 생성된 아이템
     */
    SessionItemsDTO addItem(Long sessionId, int turn, String itemName, String itemDescription, ItemGrade grade);

    /**
     * 아이템 버림 처리 (이름 매칭, 없으면 null)
     * @param sessionId 세션 PK
     * @param itemName  버릴 아이템 이름
     * @param turn      버린 턴
     * @return 버려진 아이템 PK (보유 중이 아니면 null)
     */
    Long dropItem(Long sessionId, String itemName, int turn);

    /**
     * 보유 중 인벤토리 조회
     * @param sessionId 세션 PK
     * @return 보유 아이템 목록
     */
    List<SessionItemsDTO> getInventory(Long sessionId);
}
