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
     * 해당 세계관의 활성화 된 페르소나 전체 조회
     * @param worldId 세계관 PK
     * @return 해당 세계관 페르소나
     */
    List<PersonasDTO> getPersonasByWorldId(Long worldId);

    /**
     * 페르소나 추가
     * @param personasDTO 페르소나 정보
     */
    void addPersona(PersonasDTO personasDTO);

    /**
     * 페르소나 수정
     * @param personasDTO 페르소나 정보를 바꿀 DTO
     */
    void modifyPersona(PersonasDTO personasDTO);

    /**
     * 페르소나 삭제 (CASCADE로 인해 페르소나 삭제 하면 관련 페르소나 스텟 같이 제거)
     * @param id PK
     */
    void removePersona(Long id);
}
