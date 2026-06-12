package com.worldtaletalk.story.service;

import com.worldtaletalk.stat.dto.SessionStatLogsDTO;
import com.worldtaletalk.story.dto.LlmTurnResult;
import com.worldtaletalk.story.dto.TurnProcessResult;

public interface StoryTurnTransactionService {
    /**
     * 턴 시작 처리 (세션 검증, 턴 증가, 선택지 스텟 로그 저장)
     * @param sessionId 세션 PK
     * @param choiceNumber 선택지 (자유입력시 null)
     * @return 선택지 입력시 저장된 스텟 로그 (자유입력시 null)
     */
    SessionStatLogsDTO beginTurn(Long sessionId, Integer choiceNumber);

    /**
     * 턴 완료 처리 (자유입력 스텟 로그, 아이템, 엔딩, 스냅샷, 레디스 갱신)
     * @param sessionId 세션 PK
     * @param choiceNumber 선택지 (자유입력시 null)
     * @param appliedStateLog beginTurn에서 저장된 스텟 로그 (자유입력시 null)
     * @param llmTurnResult LLM 응답 파싱 결과
     * @return 턴 처리 결과
     */
    TurnProcessResult completeTurn(Long sessionId, Integer choiceNumber, SessionStatLogsDTO appliedStateLog, LlmTurnResult llmTurnResult);

    /**
     * 보상 처리 (LLM 요청 실패시 턴 증가와 스텟 로그 원상 복구)
     * @param sessionId 세션 PK
     * @param appliedStateLog beginTurn에서 저장된 스텟 로그 (없으면 null)
     */
    void compensateTurn(Long sessionId, SessionStatLogsDTO appliedStateLog);
}
