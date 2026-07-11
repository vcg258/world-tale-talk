package com.worldtaletalk.item.mapper;

import com.worldtaletalk.config.MapStructConfig;
import com.worldtaletalk.item.domain.itemgradeprobabilities.ItemGradeProbabilitiesEntity;
import com.worldtaletalk.item.dto.ItemGradeProbabilitiesDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = MapStructConfig.class)
public interface ItemGradeProbabilitiesMapper {
    ItemGradeProbabilitiesDTO toDTO(ItemGradeProbabilitiesEntity itemGradeProbabilitiesEntity);
    List<ItemGradeProbabilitiesDTO> toDTOList(List<ItemGradeProbabilitiesEntity> itemGradeProbabilitiesEntity);
}
