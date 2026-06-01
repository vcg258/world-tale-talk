package com.worldtaletalk.world.service;

import com.worldtaletalk.world.dto.WorldsDTO;

import java.util.List;

public interface WorldsService {
    /**
     * 세계관 전체 조회
     * @return 세계관들
     */
    List<WorldsDTO> getAllWorlds();

    /**
     * 세계관 단건 조회
     * @param id PK
     * @return 세계관
     */
    WorldsDTO getWorldById(Long id);

    /**
     * 세계관 생성
     * @param worldsDTO 세계관 정보
     */
    void addWorld(WorldsDTO worldsDTO);

    /**
     * 세꼐관 수정
     * @param worldsDTO 수정할 세계관 정보
     */
    void modifyWorld(WorldsDTO worldsDTO);
}
