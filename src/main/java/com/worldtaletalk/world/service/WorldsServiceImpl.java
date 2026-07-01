package com.worldtaletalk.world.service;

import com.worldtaletalk.story.service.StoryAiService;
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
    private final StoryAiService storyAiService;

    /**
     * 세계관 전체 조회 비활성화 포함 (관리자용)
     *
     * @return 세계관들
     */
    @Transactional(readOnly = true)
    @Override
    public List<WorldsDTO> getAllWorlds() {
        return worldsMapper.toDTOList(worldsRepository.findAll());
    }

    /**
     * 세계관 단건 조회
     *
     * @param id PK
     * @return 세계관
     */
    @Transactional(readOnly = true)
    @Override
    public WorldsDTO getWorldById(Long id) {
        return worldsRepository.findById(id).map(worldsMapper::toDTO)
                .orElseThrow(() -> new NoSuchElementException("지정한 세계관이 없습니다."));
    }

    /**
     * 세계관 단건 조회 (활성화)
     *
     * @param id PK
     * @return 세계관
     */
    @Override
    public WorldsDTO getWorldByActiveId(Long id) {
        return worldsRepository.findByIdAndActiveTrue(id).map(worldsMapper::toDTO).orElseThrow(() ->
                new NoSuchElementException("지정한 세계관이 없습니다."));
    }

    /**
     * 활성화 된 세계관만 조회
     *
     * @return 활성화 세계관
     */
    @Transactional(readOnly = true)
    @Override
    public List<WorldsDTO> getActiveWorlds() {
        return worldsMapper.toDTOList(worldsRepository.findAllByActiveTrue());
    }

    /**
     * 세계관 플레이수를 기준으로 정렬 조회 (인기 상위 세게관을 표시 하기 위함)
     *
     * @return 플레이수를 기준 내림차순 정렬 세계관
     */
    @Transactional(readOnly = true)
    @Override
    public List<WorldsDTO> getFeaturedWorlds(int limit) {
        return worldsMapper.toDTOList(worldsRepository.findAllByPlayCount(limit));
    }

    /**
     * 세계관 생성
     *
     * @param worldsDTO 세계관 정보
     */
    @Transactional
    @Override
    public void addWorld(WorldsDTO worldsDTO) {
        if (worldsDTO.getName() == null || worldsDTO.getName().trim().isBlank()) {
            throw new IllegalArgumentException("세계관 이름은 필수 값입니다.");
        }
        if (worldsDTO.getGenre() == null) {
            throw new IllegalArgumentException("세계관 장르는 필수 값입니다.");
        }
        if (worldsDTO.getDescription() == null || worldsDTO.getDescription().trim().isBlank()) {
            throw new IllegalArgumentException("세계관 설명은 필수 값입니다.");
        }
        if (worldsDTO.getSettingText() == null || worldsDTO.getSettingText().trim().isBlank()) {
            throw new IllegalArgumentException("세계관 프롬프트는 필수 값입니다.");
        }
        if (worldsDTO.getTargetTurn() < 10) { // TODO 필요시 수정
            throw new IllegalArgumentException("목표 턴 수는 10 이상이어야 합니다.");
        }
        WorldsEntity world = WorldsEntity.createWorld(
                worldsDTO.getName(),
                worldsDTO.getGenre(),
                worldsDTO.getDescription(),
                worldsDTO.getSettingText(),
                worldsDTO.getTargetTurn(),
                worldsDTO.getActive()
        );

        WorldsEntity save = worldsRepository.save(world);
        storyAiService.indexWorld(save.getId()); // ETL : 벡터 인덱스 생성
        log.info("세계관 생성완료 : {}", save);
    }

    /**
     * 세계관 수정
     *
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
        storyAiService.indexWorld(save.getId()); // ETL : 벡터 인덱스 갱신 (기존 청크 삭제 후 재등록)
        log.info("세계관 수정 내역 : {}",  save);
    }

    /**
     * 세계관 삭제
     *
     * @param id PK
     */
    @Transactional
    @Override
    public void deleteWorld(Long id) {
        WorldsEntity world = worldsRepository.findById(id).orElseThrow(() ->
                new NoSuchElementException("삭제할 세계관을 찾을 수 없습니다."));
        worldsRepository.delete(world);
        storyAiService.deleteWorldIndex(id); // ETL : 벡터 인덱스 삭제
        log.info("세계관 삭제 : {}", id);
    }
}
