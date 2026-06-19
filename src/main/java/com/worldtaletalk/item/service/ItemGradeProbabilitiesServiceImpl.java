package com.worldtaletalk.item.service;

import com.worldtaletalk.item.domain.itemgradeprobabilities.ItemGradeProbabilitiesEntity;
import com.worldtaletalk.item.domain.itemgradeprobabilities.PhaseType;
import com.worldtaletalk.item.dto.ItemGradeProbabilitiesDTO;
import com.worldtaletalk.item.mapper.ItemGradeProbabilitiesMapper;
import com.worldtaletalk.item.repository.ItemGradeProbabilitiesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemGradeProbabilitiesServiceImpl implements ItemGradeProbabilitiesService {
    private final ItemGradeProbabilitiesRepository itemGradeProbabilitiesRepository;
    private final ItemGradeProbabilitiesMapper itemGradeProbabilitiesMapper;

    /**
     * 전체 확률 조회
     *
     * @return 전체 구간 확률 목록
     */
    @Transactional(readOnly = true)
    @Override
    public List<ItemGradeProbabilitiesDTO> getProbabilities() {
        List<ItemGradeProbabilitiesEntity> entities = itemGradeProbabilitiesRepository.findAll();
        return itemGradeProbabilitiesMapper.toDTOList(entities);
    }

    /**
     * 구간별 확률 조회
     *
     * @param phase 진행도 구간 (EARLY, MID, LATE)
     * @return 해당 구간 확률 목록
     */
    @Transactional(readOnly = true)
    @Override
    public List<ItemGradeProbabilitiesDTO> getProbabilitiesByPhase(PhaseType phase) {
        List<ItemGradeProbabilitiesEntity> entities = itemGradeProbabilitiesRepository.findAllByPhase(phase);
        return itemGradeProbabilitiesMapper.toDTOList(entities);
    }

    /**
     * 단건 확률 수정 (관리자)
     *
     * @param id          확률 설정 PK
     * @param probability 수정할 확률 값 (0~100)
     * @return 수정된 확률 설정
     */
    @Transactional
    @Override
    public ItemGradeProbabilitiesDTO modifyProbability(Long id, Integer probability) {
        ItemGradeProbabilitiesEntity entity = itemGradeProbabilitiesRepository.findById(id).orElseThrow(() ->
                new NoSuchElementException("해당 확률 설정이 존재하지 않습니다."));

        entity.updateProbability(probability != null ? probability : entity.getProbability());

        ItemGradeProbabilitiesEntity saved = itemGradeProbabilitiesRepository.save(entity);
        log.info("확률 수정 완료 phase: {}, grade: {}, probability: {}",
                saved.getPhase(), saved.getGrade(), saved.getProbability());
        return itemGradeProbabilitiesMapper.toDTO(saved);
    }
}
