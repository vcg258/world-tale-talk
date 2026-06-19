package com.worldtaletalk.persona.service;

import com.worldtaletalk.persona.dto.PersonasBaseStatsDTO;

import java.util.List;

public interface PersonasBaseStatsService {
    /**
     * 페르소나 스텟 여러건 조회
     *
     * @return 페르소나 스텟
     */
    List<PersonasBaseStatsDTO> getPersonasBaseStatsList();

    /**
     * 페르소나 스텟 단건 조회
     *
     * @param id PK
     * @return 페르소나 스텟
     */
    PersonasBaseStatsDTO getPersonasBaseStats(Long id);

    /**
     * FK로 페르소나 스텟 단건 조회
     *
     * @param id FK
     * @return 페르소나 스텟
     */
    PersonasBaseStatsDTO getPersonasBaseStatsByPersonaId(Long id);

    /**
     * 해당 페르소나 기본 스텟 생성
     *
     * @param personasBaseStatsDTO 페르소나 기본스텟 정보
     */
    void addPersonaBaseStats(PersonasBaseStatsDTO personasBaseStatsDTO);

    /**
     * 해당 페르소나 스텟 수정
     *
     * @param personasBaseStatsDTO 기본스텟 정보
     */
    void modifyPersonaBaseStats(PersonasBaseStatsDTO personasBaseStatsDTO);
}
