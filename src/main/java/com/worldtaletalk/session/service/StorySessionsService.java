package com.worldtaletalk.session.service;

import com.worldtaletalk.session.dto.StorySessionsDTO;

import java.util.List;

public interface StorySessionsService {
    /**
     * 세션 여러건 조회
     * @return 세션s
     */
    List<StorySessionsDTO> getStorySessions();

    /**
     * 세션 단건 조회
     * @param id PK
     * @return 세션
     */
    StorySessionsDTO getStorySession(Long id);

    /**
     * 세션 생성 (새게임)
     * @param storySessionsDTO 세션 정보
     */
    void createStorySession(StorySessionsDTO storySessionsDTO);


}
