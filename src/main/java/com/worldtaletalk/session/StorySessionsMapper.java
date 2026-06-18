package com.worldtaletalk.session;

import com.worldtaletalk.session.domain.StorySessionsEntity;
import com.worldtaletalk.session.dto.StorySessionsDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StorySessionsMapper {
    @Mapping(source = "users.id", target = "userId")
    @Mapping(source = "worlds.id", target = "worldId")
    @Mapping(source = "worlds.name", target = "worldName")
    @Mapping(source = "personas.id", target = "personaId")
    StorySessionsDTO toDTO(StorySessionsEntity storySessionsEntity);
    List<StorySessionsDTO> toDTOList(List<StorySessionsEntity> storySessionsEntityList);
}
