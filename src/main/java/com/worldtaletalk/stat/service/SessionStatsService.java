package com.worldtaletalk.stat.service;

import com.worldtaletalk.stat.dto.SessionStatLogsDTO;
import com.worldtaletalk.stat.dto.SessionStatSnapshotsDTO;

import java.util.Map;

public interface SessionStatsService {

    /**
     * 스텟 변화 로그 생성
     *
     * @param sessionStatLogsDTO 턴별 스텟 변화량 정보
     * @return 저장된 스텟 로그
     */
    SessionStatLogsDTO addStatLog(SessionStatLogsDTO sessionStatLogsDTO);

    /**
     * 현재 스텟 합산 조회 (기본 스텟 + 모든 로그 변화량 누적)
     *
     * @param sessionId 세션 PK
     * @return 스텟별 합산 값 (wisdom, strength, agility, charisma, luck)
     */
    Map<String, Integer> getCurrentStats(Long sessionId);

    /**
     * 엔딩 시점 스텟 스냅샷 생성
     *
     * @param sessionId 세션 PK
     * @return 저장된 스냅샷
     */
    SessionStatSnapshotsDTO addSnapshot(Long sessionId);

    /**
     * 스텟 로그 삭제 (LLM호출 실패시 보상용)
     *
     * @param statLogId 스텟 로그 PK
     */
    void deleteStatLog(Long statLogId);

    /**
     * 엔딩 이후 메인스토리 요약
     *
     * @param sessionId 세션 PK
     * @param summary 요약
     */
    void modifySummary(Long sessionId, String summary);
}
