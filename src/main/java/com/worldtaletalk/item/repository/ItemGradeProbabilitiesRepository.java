package com.worldtaletalk.item.repository;

import com.worldtaletalk.item.domain.itemgradeprobabilities.ItemGradeProbabilitiesEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemGradeProbabilitiesRepository extends JpaRepository<ItemGradeProbabilitiesEntity, Long> {
}
