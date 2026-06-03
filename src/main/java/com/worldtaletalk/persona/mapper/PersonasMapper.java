package com.worldtaletalk.persona.mapper;

import com.worldtaletalk.persona.domain.PersonasEntity;
import com.worldtaletalk.persona.dto.PersonasBaseStatsDTO;
import com.worldtaletalk.persona.dto.PersonasDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PersonasMapper {
    PersonasDTO toDTO(PersonasEntity personasEntity);
    List<PersonasDTO> toDTOList(List<PersonasEntity> personasEntity);
}
