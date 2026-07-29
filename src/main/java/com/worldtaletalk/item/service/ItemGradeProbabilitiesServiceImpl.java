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

}
