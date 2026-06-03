package com.worldtaletalk.persona.service;

import com.worldtaletalk.persona.domain.PersonasBaseStatsEntity;
import com.worldtaletalk.persona.domain.PersonasEntity;
import com.worldtaletalk.persona.dto.PersonasBaseStatsDTO;
import com.worldtaletalk.persona.mapper.PersonasBaseStatsMapper;
import com.worldtaletalk.persona.repository.PersonasBaseStatsRepository;
import com.worldtaletalk.persona.repository.PersonasRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class PersonasBaseStatsServiceImpl implements PersonasBaseStatsService {
    private final PersonasBaseStatsRepository personasBaseStatsRepository;
    private final PersonasRepository personasRepository;
    private final PersonasBaseStatsMapper personasBaseStatsMapper;

    /**
     * 페르소나 스텟 여러건 조회
     * @return 페르소나 스텟
     */
    @Override
    public List<PersonasBaseStatsDTO> getPersonasBaseStatsList() {
        List<PersonasBaseStatsEntity> personasBaseStatsList = personasBaseStatsRepository.findAll();
        return personasBaseStatsMapper.toDTOList(personasBaseStatsList);
    }

    /**
     * 페르소나 스텟 단건 조회
     * @param id PK
     * @return 페르소나 스텟
     */
    @Override
    public PersonasBaseStatsDTO getPersonasBaseStats(Long id) {
        PersonasBaseStatsEntity baseStats = personasBaseStatsRepository.findById(id).orElseThrow(() ->
                new IllegalStateException("해당 페르소나의 스텟이 존재하지 않음"));
        return personasBaseStatsMapper.toDTO(baseStats);
    }

    /**
     * 해당 페르소나 기본 스텟 생성
     * @param personasBaseStatsDTO 페르소나 기본스텟 정보
     */
    @Override
    public void addPersonaBaseStats(PersonasBaseStatsDTO personasBaseStatsDTO) {
        PersonasEntity persona = personasRepository.findById(personasBaseStatsDTO.getPersonaId()).orElseThrow(() ->
                new IllegalStateException("해당 페르소나 없음"));
        PersonasBaseStatsEntity baseStats = PersonasBaseStatsEntity.createPersonaBaseStats(
                personasBaseStatsDTO.getWisdom(),
                personasBaseStatsDTO.getStrength(),
                personasBaseStatsDTO.getAgility(),
                personasBaseStatsDTO.getCharisma(),
                personasBaseStatsDTO.getLuck(),
                persona
        );

        log.info("페르소나 : {}, 스텟 : {}", persona.getName(), baseStats);
        personasBaseStatsRepository.save(baseStats);
    }
}
