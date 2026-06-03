package com.worldtaletalk.persona.service;

import com.worldtaletalk.persona.domain.PersonasEntity;
import com.worldtaletalk.persona.dto.PersonasDTO;
import com.worldtaletalk.persona.mapper.PersonasMapper;
import com.worldtaletalk.persona.repository.PersonasRepository;
import com.worldtaletalk.world.domain.WorldsEntity;
import com.worldtaletalk.world.repository.WorldsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class PersonasServiceImpl implements PersonasService {
    private final PersonasRepository personasRepository;
    private final PersonasMapper personasMapper;
    private final WorldsRepository worldsRepository;

    /**
     * 페르소나 여러건 조회
     * @return 페르소나
     */
    @Override
    public List<PersonasDTO> getPersonasList() {
        List<PersonasEntity> personasList = personasRepository.findAll();
        return personasMapper.toDTOList(personasList);
    }

    /**
     * 페르소나 단건 조회
     * @param id FK
     * @return 페르소나
     */
    @Override
    public PersonasDTO getPersona(Long id) {
        PersonasEntity persona = personasRepository.findById(id).orElseThrow(() ->
                new IllegalStateException("해당 페르소나 없음"));
        return personasMapper.toDTO(persona);
    }

    /**
     * 페르소나 추가
     * @param personasDTO 페르소나 정보
     */
    @Override
    public void addPersona(PersonasDTO personasDTO) {
        WorldsEntity worldsEntity = worldsRepository.findById(personasDTO.getWorldId()).orElseThrow(() ->
                new IllegalStateException("해당 세계관은 존재하지 않음"));
        PersonasEntity persona = PersonasEntity.addPersona(
                worldsEntity,
                personasDTO.getName(),
                personasDTO.getDescription(),
                personasDTO.getBackground(),
                personasDTO.getActive()
        );
        log.info("페르소나 등록 : {}", persona);
        personasRepository.save(persona);
    }
}
