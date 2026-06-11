package com.worldtaletalk.stat.mapper;

import com.worldtaletalk.stat.domain.SessionStatSnapshotsEntity;
import com.worldtaletalk.stat.dto.SessionStatSnapshotsDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SessionStatSnapshotsMapper {
    @Mapping(source = "storySession.id", target = "storySessionId")
    SessionStatSnapshotsDTO toDTO(SessionStatSnapshotsEntity sessionStatSnapshotsEntity);
}
