package com.worldtaletalk.session.service;

import com.worldtaletalk.persona.domain.PersonasEntity;
import com.worldtaletalk.persona.repository.PersonasRepository;
import com.worldtaletalk.session.StorySessionsMapper;
import com.worldtaletalk.session.domain.StorySessionsEntity;
import com.worldtaletalk.session.dto.StorySessionsDTO;
import com.worldtaletalk.session.repository.StorySessionsRepository;
import com.worldtaletalk.user.domain.users.UsersEntity;
import com.worldtaletalk.user.repository.UsersRepository;
import com.worldtaletalk.world.domain.WorldsEntity;
import com.worldtaletalk.world.repository.WorldsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Log4j2
@Service
@RequiredArgsConstructor
public class StorySessionsServiceImpl implements StorySessionsService {
    private final StorySessionsRepository storySessionsRepository;
    private final StorySessionsMapper storySessionsMapper;
    private final UsersRepository usersRepository;
    private final WorldsRepository worldsRepository;
    private final PersonasRepository personasRepository;

    /**
     * 세션 여러건 조회
     * @return 세션s
     */
    @Override
    public List<StorySessionsDTO> getStorySessions() {
        List<StorySessionsEntity> storySessionsEntityList = storySessionsRepository.findAll();
        return storySessionsMapper.toDTOList(storySessionsEntityList);
    }

    /**
     * 세션 단건 조회
     * @param id PK
     * @return 세션
     */
    @Override
    public StorySessionsDTO getStorySession(Long id) {
        StorySessionsEntity storySession = storySessionsRepository.findById(id).orElseThrow(() ->
                new IllegalStateException("해당 세션은 존재 하지 않습니다."));
        return storySessionsMapper.toDTO(storySession);
    }

    /**
     * 세션 생성 (새게임)
     * @param storySessionsDTO 세션 정보
     */
    @Override
    public void createStorySession(StorySessionsDTO storySessionsDTO) {
        UsersEntity user = usersRepository.findById(storySessionsDTO.getUserId()).orElseThrow(() ->
                new IllegalStateException("해당 유저는 찾을 수 없습니다."));

        WorldsEntity world = worldsRepository.findById(storySessionsDTO.getWorldId()).orElseThrow(() ->
                new IllegalStateException("해당 세계관은 찾을 수 없습니다."));

        PersonasEntity persona = personasRepository.findById(storySessionsDTO.getPersonaId()).orElseThrow(() ->
                new IllegalStateException("해당 페르소나는 찾을 수 없습니다."));

        StorySessionsEntity storySessions = StorySessionsEntity.createSession(
                user,
                world,
                persona
        );
        storySessionsRepository.save(storySessions);
        log.info("세션 생성 : {}", storySessions);
    }
}
