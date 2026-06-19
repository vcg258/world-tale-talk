package com.worldtaletalk.chat.service;

import com.worldtaletalk.chat.dto.ChatMessagesDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatMessagesService {

    /**
     * 세션 채팅 내역 커서 기반 조회 (WebSocket 재연결시 이전 대화 복원용) (페이징)
     *
     * @param sessionId    세션 PK
     * @param lastCreatedAt 현재 화면 제일 위 메시지 createdAt (최초 요청시 null)
     * @return 채팅 메시지 목록 (최신순 20개)
     */
    List<ChatMessagesDTO> getChatMessages(Long sessionId, LocalDateTime lastCreatedAt);
}
