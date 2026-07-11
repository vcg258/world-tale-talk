package com.worldtaletalk.rating;

import com.worldtaletalk.config.MapStructConfig;
import com.worldtaletalk.rating.domain.WorldRatingsEntity;
import com.worldtaletalk.rating.dto.WorldRatingsDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = MapStructConfig.class)
public interface WorldRatingsMapper {
    @Mapping(source = "users.id", target = "userId")
    @Mapping(source = "world.id", target = "worldId")
    @Mapping(source = "storySession.id", target = "storySessionId")
    @Mapping(source = "users.nickname", target = "userNickname")
    @Mapping(source = "world.name", target = "worldName")
    WorldRatingsDTO toDTO(WorldRatingsEntity worldRatingsEntity);
    List<WorldRatingsDTO> toDTOList(List<WorldRatingsEntity> worldRatingsEntity);
}
