package com.worldtaletalk.persona.service;

import com.worldtaletalk.persona.domain.PersonasEntity;
import com.worldtaletalk.persona.dto.PersonasDTO;
import com.worldtaletalk.persona.mapper.PersonasMapper;
import com.worldtaletalk.persona.repository.PersonasRepository;
import com.worldtaletalk.session.repository.StorySessionsRepository;
import com.worldtaletalk.world.domain.WorldsEntity;
import com.worldtaletalk.world.repository.WorldsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class PersonasServiceImpl implements PersonasService {
    private final PersonasRepository personasRepository;
    private final PersonasMapper personasMapper;
    private final WorldsRepository worldsRepository;
    private final StorySessionsRepository storySessionsRepository;

    /**
     * 페르소나 여러건 조회
     *
     * @return 페르소나
     */
    @Transactional(readOnly = true)
    @Override
    public List<PersonasDTO> getPersonasList() {
        return personasMapper.toDTOList(personasRepository.findAll());
    }

    /**
     * 페르소나 단건 조회
     *
     * @param id FK
     * @return 페르소나
     */
    @Transactional(readOnly = true)
    @Override
    public PersonasDTO getPersona(Long id) {
        return personasRepository.findById(id).map(personasMapper::toDTO)
                .orElseThrow(() -> new NoSuchElementException("해당 페르소나 없음"));
    }

    /**
     * 해당 세계관의 활성화 된 페르소나 전체 조회
     *
     * @param worldId 세계관 PK
     * @return 해당 세계관 페르소나
     */
    @Transactional(readOnly = true)
    @Override
    public List<PersonasDTO> getPersonasByWorldId(Long worldId) {
        return personasMapper.toDTOList(personasRepository.findAllByWorldIdAndActiveTrue(worldId));
    }

    /**
     * 해당 세계관 페르소나 전체 조회 (비활성 포함, 관리자용)
     *
     * @param worldId 세계관 PK
     * @return 해당 세계관 페르소나 전체 목록
     */
    @Transactional(readOnly = true)
    @Override
    public List<PersonasDTO> getPersonasAllByWorldId(Long worldId) {
        return personasMapper.toDTOList(personasRepository.findAllByWorldId(worldId));
    }

    /**
     * 해당 세계관 페르소나 + 기본 스탯 포함 전체 조회
     *
     * @param worldId 세계관 PK
     * @return 페르소나 + 기본 스텟 목록
     */
    @Transactional(readOnly = true)
    @Override
    public List<PersonasDTO> getPersonasByWorldIdWithBaseStats(Long worldId) {
        return personasMapper.toDTOList(personasRepository.findAllByWorldIdWithBaseStats(worldId));
    }

    /**
     * 페르소나 추가
     *
     * @param personasDTO 페르소나 정보
     * @return 생성된 페르소나 PK
     */
    @Transactional
    @Override
    public Long addPersona(PersonasDTO personasDTO) {
        WorldsEntity worldsEntity = worldsRepository.findById(personasDTO.getWorldId()).orElseThrow(() ->
                new NoSuchElementException("해당 세계관은 존재하지 않음"));
        PersonasEntity persona = PersonasEntity.addPersona(
                worldsEntity,
                personasDTO.getName(),
                personasDTO.getDescription(),
                personasDTO.getBackground(),
                personasDTO.getActive()
        );
        PersonasEntity save = personasRepository.save(persona);
        log.info("페르소나 등록 : {}", save);
        return save.getId();
    }

    /**
     * 페르소나 수정
     *
     * @param personasDTO 페르소나 정보를 바꿀 DTO
     */
    @Transactional
    @Override
    public void modifyPersona(PersonasDTO personasDTO) {
        PersonasEntity persona = personasRepository.findById(personasDTO.getId()).orElseThrow(() ->
                new NoSuchElementException("해당 페르소나는 존재하지 않습니다."));

        WorldsEntity world = persona.getWorld();

        // 매개변수 perosna.getWorld() 가 null이 아닐경우 (값을 안넣으면 기존 값 유지를 위함)
        if (personasDTO.getWorldId() != null) {
            world = worldsRepository.findById(personasDTO.getWorldId()).orElseThrow(() ->
                    new NoSuchElementException("해당 세계관은 존재하지 않습니다."));
        }

        persona.modifyInformationByPersona(
                world,
                personasDTO.getName() != null ? personasDTO.getName() : persona.getName(),
                personasDTO.getDescription() != null ? personasDTO.getDescription() : persona.getDescription(),
                personasDTO.getBackground()  != null ? personasDTO.getBackground() : persona.getBackground(),
                personasDTO.getActive() != null ? personasDTO.getActive() : persona.isActive()
        );
        PersonasEntity save = personasRepository.save(persona);
        log.info("페르소나 수정 완료 : {}", save);
    }

    /**
     * 페르소나 삭제 (CASCADE로 인해 페르소나 삭제 하면 관련 페르소나 스텟 같이 제거)
     *
     * @param id PK
     */
    @Transactional
    @Override
    public void deletePersona(Long id) {
        PersonasEntity persona = personasRepository.findById(id).orElseThrow(() ->
                new NoSuchElementException("삭제할 페르소나를 찾을 수 없습니다."));
        if (storySessionsRepository.existsByPersonasId(id)) {
            throw new IllegalStateException("해당 페르소나로 진행된 세션이 있어 삭제할 수 없습니다.");
        }
        personasRepository.delete(persona);
        log.info("페르소나 제거 완료 personaId : {}", id);
    }

    /**
     * 페르소나 삭제 대기 처리 (유예기간 시작, 배치가 이후 최종 삭제 여부 판단)
     *
     * @param id PK
     */
    @Transactional
    @Override
    public void schedulePersonaDeletion(Long id) {
        PersonasEntity persona = personasRepository.findById(id).orElseThrow(() ->
                new NoSuchElementException("삭제 대기 처리할 페르소나를 찾을 수 없습니다."));
        if (persona.isActive()) {
            throw new IllegalStateException("비활성화된 페르소나만 삭제 대기 처리할 수 있습니다.");
        }
        persona.scheduleDeletion();
        PersonasEntity save = personasRepository.save(persona);
        log.info("페르소나 삭제 대기 처리 : {}", save);
    }

    /**
     * 페르소나 삭제 대기 취소
     *
     * @param id PK
     */
    @Transactional
    @Override
    public void cancelPersonaDeletion(Long id) {
        PersonasEntity persona = personasRepository.findById(id).orElseThrow(() ->
                new NoSuchElementException("삭제 대기 취소할 페르소나를 찾을 수 없습니다."));
        persona.cancelDeletion();
        PersonasEntity save = personasRepository.save(persona);
        log.info("페르소나 삭제 대기 취소 : {}", save);
    }
}
