package com.worldtaletalk.world.service;

import com.worldtaletalk.world.WorldsMapper;
import com.worldtaletalk.world.domain.WorldsEntity;
import com.worldtaletalk.world.dto.WorldsDTO;
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
public class WorldsServiceImpl implements WorldsService {
    private final WorldsRepository worldsRepository;
    private final WorldsMapper worldsMapper;

    /**
     * 세계관 전체 조회
     * @return 세계관들
     */
    @Override
    public List<WorldsDTO> getAllWorlds() {
        List<WorldsEntity> worlds = worldsRepository.findAll();
        return worldsMapper.toDTOList(worlds);
    }

    /**
     * 세계관 단건 조회
     * @param id PK
     * @return 세계관
     */
    @Override
    public WorldsDTO getWorldById(Long id) {
        WorldsEntity world = worldsRepository.findById(id).orElseThrow(() ->
                new NoSuchElementException("지정한 세계관이 없습니다."));

        return worldsMapper.toDTO(world);
    }

    /**
     * 세계관 생성
     * @param worldsDTO 세계관 정보
     */
    @Override
    public void addWorld(WorldsDTO worldsDTO) {
        WorldsEntity world = WorldsEntity.createWorld(
                worldsDTO.getName(),
                worldsDTO.getGenre(),
                worldsDTO.getDescription(),
                worldsDTO.getSettingText(),
                worldsDTO.getTargetTurn(),
                worldsDTO.getActive()
        );

        WorldsEntity save = worldsRepository.save(world);
        log.info("세계관 생성완료 : {}", save);
    }

    /**
     * 세꼐관 수정
     * @param worldsDTO 수정할 세계관 정보
     */
    @Transactional
    @Override
    public void modifyWorld(WorldsDTO worldsDTO) {
        WorldsEntity worlds = worldsRepository.findById(worldsDTO.getId()).orElseThrow(() ->
                new NoSuchElementException("수정할 세계관을 찾을 수 없습니다."));

        worlds.modifyInformationByWorld(
                worldsDTO.getName() != null ? worldsDTO.getName() : worlds.getName(),
                worldsDTO.getGenre() != null ? worldsDTO.getGenre() : worlds.getGenre(),
                worldsDTO.getDescription() != null ? worldsDTO.getDescription() : worlds.getDescription(),
                worldsDTO.getSettingText() != null ? worldsDTO.getSettingText() : worlds.getSettingText(),
                worldsDTO.getTargetTurn() != null ? worldsDTO.getTargetTurn() : worlds.getTargetTurn(),
                worldsDTO.getActive() != null ? worldsDTO.getActive() : worlds.isActive()
        );

        WorldsEntity save = worldsRepository.save(worlds);
        log.info("세계관 수정 내역 : {}",  save);
    }

    /**
     * 세계관 삭제
     * @param id PK
     */
    @Override
    public void deleteWorld(Long id) {
        worldsRepository.deleteById(id);
        log.info("세계관 삭제 : {}", id);
    }
}
