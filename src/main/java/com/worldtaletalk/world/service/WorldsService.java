package com.worldtaletalk.world.service;

import com.worldtaletalk.world.dto.WorldsDTO;

import java.util.List;

public interface WorldsService {
    /**
     * 세계관 전체 조회 비활성화 포함 (관리자용)
     *
     * @return 세계관들
     */
    List<WorldsDTO> getAllWorlds();

    /**
     * 세계관 단건 조회
     *
     * @param id PK
     * @return 세계관
     */
    WorldsDTO getWorldById(Long id);

    /**
     * 세계관 단건 조회 (활성화)
     *
     * @param id PK
     * @return 세계관
     */
    WorldsDTO getWorldByActiveId(Long id);

    /**
     * 활성화 된 세계관만 조회
     *
     * @return 활성화 세계관
     */
    List<WorldsDTO> getActiveWorlds();

    /**
     * 세계관 플레이수를 기준으로 정렬 조회 (인기 상위 세게관을 표시 하기 위함)
     *
     * @return 플레이수를 기준 내림차순 정렬 세계관
     */
    List<WorldsDTO> getFeaturedWorlds(int limit);

    /**
     * 세계관 생성
     *
     * @param worldsDTO 세계관 정보
     */
    void addWorld(WorldsDTO worldsDTO);

    /**
     * 세계관 수정
     *
     * @param worldsDTO 수정할 세계관 정보
     */
    void modifyWorld(WorldsDTO worldsDTO);

    /**
     * 세계관 삭제
     *
     * @param id PK
     */
    void deleteWorld(Long id);

    /**
     * 세계관 삭제 대기 처리 (유예기간 시작, 배치가 이후 최종 삭제 여부 판단)
     *
     * @param id PK
     */
    void scheduleWorldDeletion(Long id);

    /**
     * 세계관 삭제 대기 취소
     *
     * @param id PK
     */
    void cancelWorldDeletion(Long id);
}
