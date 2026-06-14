package com.worldtaletalk.item.repository;

import com.worldtaletalk.item.domain.itemgradeprobabilities.ItemGradeProbabilitiesEntity;
import com.worldtaletalk.item.domain.itemgradeprobabilities.PhaseType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemGradeProbabilitiesRepository extends JpaRepository<ItemGradeProbabilitiesEntity, Long> {
    // 진행도로 조회
    List<ItemGradeProbabilitiesEntity> findAllByPhase(PhaseType phaseType);
}
