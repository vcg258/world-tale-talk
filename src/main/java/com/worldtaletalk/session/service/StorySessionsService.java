package com.worldtaletalk.session.service;

import com.worldtaletalk.session.domain.EndingType;
import com.worldtaletalk.session.domain.SessionStatus;
import com.worldtaletalk.session.dto.StorySessionsDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface StorySessionsService {
    /**
     * 세션 여러건 조회 (관리자용)
     *
     * @return 세션s
     */
    List<StorySessionsDTO> getStorySessions();

    /**
     * 세션 단건 조회
     *
     * @param id PK
     * @return 세션
     */
    StorySessionsDTO getStorySession(Long id);

    /**
     * 해당 회원의 세션 여러건 조회
     *
     * @param userId 회원 PK
     * @param page 페이지 수
     * @return 회원 세션 목록
     */
    Page<StorySessionsDTO> getMySessions(Long userId, SessionStatus status, int page);

    /**
     * 세션 생성 (새게임)
     *
     * @param storySessionsDTO 세션 정보
     * @return 생성된 세션 PK
     */
    Long createStorySession(StorySessionsDTO storySessionsDTO);

    /**
     * 세션상태 변경
     *
     * @param id PK
     * @param status 새션 상태
     */
    void modifyStorySessionByStatus(Long id, SessionStatus status);

    /**
     * 세션 엔딩 결정
     *
     * @param id PK
     * @param endingType 엔딩
     */
    void modifyStorySessionByEndingType(Long id, EndingType endingType);

    /**
     * 세션 삭제
     *
     * @param id PK
     */
    void deleteStorySessionByStatus(Long id);
}
