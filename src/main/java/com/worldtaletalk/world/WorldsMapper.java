package com.worldtaletalk.world;

import com.worldtaletalk.config.MapStructConfig;
import com.worldtaletalk.world.domain.WorldsEntity;
import com.worldtaletalk.world.dto.WorldsDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = MapStructConfig.class)
public interface WorldsMapper {
    WorldsDTO toDTO(WorldsEntity worldsEntity);
    List<WorldsDTO> toDTOList(List<WorldsEntity> worldsEntityList);

}
