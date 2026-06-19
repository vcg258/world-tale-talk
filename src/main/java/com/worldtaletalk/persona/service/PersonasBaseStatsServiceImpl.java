package com.worldtaletalk.persona.service;

import com.worldtaletalk.persona.domain.PersonasBaseStatsEntity;
import com.worldtaletalk.persona.domain.PersonasEntity;
import com.worldtaletalk.persona.dto.PersonasBaseStatsDTO;
import com.worldtaletalk.persona.mapper.PersonasBaseStatsMapper;
import com.worldtaletalk.persona.repository.PersonasBaseStatsRepository;
import com.worldtaletalk.persona.repository.PersonasRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class PersonasBaseStatsServiceImpl implements PersonasBaseStatsService {
    private final PersonasBaseStatsRepository personasBaseStatsRepository;
    private final PersonasRepository personasRepository;
    private final PersonasBaseStatsMapper personasBaseStatsMapper;

    /**
     * 페르소나 스텟 여러건 조회
     *
     * @return 페르소나 스텟
     */
    @Transactional(readOnly = true)
    @Override
    public List<PersonasBaseStatsDTO> getPersonasBaseStatsList() {
        return personasBaseStatsMapper.toDTOList(personasBaseStatsRepository.findAll());
    }

    /**
     * 페르소나 스텟 단건 조회
     *
     * @param id PK
     * @return 페르소나 스텟
     */
    @Transactional(readOnly = true)
    @Override
    public PersonasBaseStatsDTO getPersonasBaseStats(Long id) {
        return personasBaseStatsRepository.findById(id).map(personasBaseStatsMapper::toDTO)
                .orElseThrow(() -> new NoSuchElementException("해당 스텟이 존재하지 않음"));
    }

    /**
     * FK로 페르소나 스텟 단건 조회
     *
     * @param id FK
     * @return 페르소나 스텟
     */
    @Transactional(readOnly = true)
    @Override
    public PersonasBaseStatsDTO getPersonasBaseStatsByPersonaId(Long id) {
        return personasBaseStatsRepository.findByPersonaId(id).map(personasBaseStatsMapper::toDTO)
                .orElseThrow(() -> new NoSuchElementException("해당 페르소나의 스텟이 존재하지 않음"));
    }

    /**
     * 해당 페르소나 기본 스텟 생성
     *
     * @param personasBaseStatsDTO 페르소나 기본스텟 정보
     */
    @Transactional
    @Override
    public void addPersonaBaseStats(PersonasBaseStatsDTO personasBaseStatsDTO) {
        PersonasEntity persona = personasRepository.findById(personasBaseStatsDTO.getPersonaId()).orElseThrow(() ->
                new NoSuchElementException("해당 페르소나 없음"));

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

    /**
     * 해당 페르소나 스텟 수정
     *
     * @param personasBaseStatsDTO 기본스텟 정보
     */
    @Transactional
    @Override
    public void modifyPersonaBaseStats(PersonasBaseStatsDTO personasBaseStatsDTO) {

        PersonasBaseStatsEntity baseStats = personasBaseStatsRepository.findById(personasBaseStatsDTO.getId()).orElseThrow(() ->
                new NoSuchElementException("해당 페르소나 스텟이 존재 하지 않음"));

        baseStats.modifyInformation(
                personasBaseStatsDTO.getWisdom() != null ? personasBaseStatsDTO.getWisdom() : baseStats.getWisdom(),
                personasBaseStatsDTO.getStrength() != null ? personasBaseStatsDTO.getStrength() : baseStats.getStrength(),
                personasBaseStatsDTO.getAgility() != null ? personasBaseStatsDTO.getAgility() : baseStats.getAgility(),
                personasBaseStatsDTO.getCharisma() != null ? personasBaseStatsDTO.getCharisma() : baseStats.getCharisma(),
                personasBaseStatsDTO.getLuck() != null ? personasBaseStatsDTO.getLuck() : baseStats.getLuck()
        );
        log.info("페르소나 스텟 수정 요청 : {}", baseStats);
        personasBaseStatsRepository.save(baseStats);
    }
}