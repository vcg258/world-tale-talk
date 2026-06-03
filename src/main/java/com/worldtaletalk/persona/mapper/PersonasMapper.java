package com.worldtaletalk.persona.mapper;

import com.worldtaletalk.persona.domain.PersonasEntity;
import com.worldtaletalk.persona.dto.PersonasDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PersonasMapper {
    @Mapping(source = "world.id", target = "worldId")
    PersonasDTO toDTO(PersonasEntity personasEntity);
    List<PersonasDTO> toDTOList(List<PersonasEntity> personasEntity);
}
