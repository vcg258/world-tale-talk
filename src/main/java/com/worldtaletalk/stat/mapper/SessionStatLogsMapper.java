package com.worldtaletalk.stat.mapper;

import com.worldtaletalk.stat.domain.SessionStatLogsEntity;
import com.worldtaletalk.stat.dto.SessionStatLogsDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SessionStatLogsMapper {
    @Mapping(source = "storySession.id", target = "storySessionId")
    SessionStatLogsDTO toDTO(SessionStatLogsEntity sessionStatLogsEntity);
    List<SessionStatLogsDTO> toDTOList(List<SessionStatLogsEntity> sessionStatLogsEntityList);
}
