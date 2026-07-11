package com.worldtaletalk.persona.mapper;

import com.worldtaletalk.config.MapStructConfig;
import com.worldtaletalk.persona.domain.PersonasBaseStatsEntity;
import com.worldtaletalk.persona.dto.PersonasBaseStatsDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = MapStructConfig.class)
public interface PersonasBaseStatsMapper {
    @Mapping(source = "persona.id", target = "personaId")
    PersonasBaseStatsDTO toDTO(PersonasBaseStatsEntity personasBaseStatsEntity);
    List<PersonasBaseStatsDTO> toDTOList(List<PersonasBaseStatsEntity> personasBaseStatsEntity);
}
