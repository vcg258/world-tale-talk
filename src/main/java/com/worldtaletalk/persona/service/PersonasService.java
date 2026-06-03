package com.worldtaletalk.persona.service;

import com.worldtaletalk.persona.dto.PersonasDTO;

import java.util.List;

public interface PersonasService {
    /**
     * 페르소나 여러건 조회
     * @return 페르소나
     */
    List<PersonasDTO> getPersonasList();

    /**
     * 페르소나 단건 조회
     * @param id FK
     * @return 페르소나
     */
    PersonasDTO getPersona(Long id);

    /**
     * 페르소나 추가
     * @param personasDTO 페르소나 정보
     */
    void addPersona(PersonasDTO personasDTO);
}
