package com.worldtaletalk.item.service;

import com.worldtaletalk.item.domain.itemgradeprobabilities.PhaseType;
import com.worldtaletalk.item.dto.ItemGradeProbabilitiesDTO;

import java.util.List;

public interface ItemGradeProbabilitiesService {

    // 전체 확률 조회
    List<ItemGradeProbabilitiesDTO> getProbabilities();

    // 구간별 확률 조회
    List<ItemGradeProbabilitiesDTO> getProbabilitiesByPhase(PhaseType phase);

    // 단건 확률 수정
    ItemGradeProbabilitiesDTO modifyProbability(Long id, Integer probability);
}
