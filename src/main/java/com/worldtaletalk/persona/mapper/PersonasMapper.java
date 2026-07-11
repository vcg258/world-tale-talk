package com.worldtaletalk.persona.mapper;

import com.worldtaletalk.config.MapStructConfig;
import com.worldtaletalk.persona.domain.PersonasEntity;
import com.worldtaletalk.persona.dto.PersonasDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = MapStructConfig.class, uses = PersonasBaseStatsMapper.class)
public interface PersonasMapper {
    @Mapping(source = "world.id", target = "worldId")
    @Mapping(source = "personaBaseStats", target = "baseStats")
    PersonasDTO toDTO(PersonasEntity personasEntity);
    List<PersonasDTO> toDTOList(List<PersonasEntity> personasEntity);
}
