package com.worldtaletalk.inventory;

import com.worldtaletalk.inventory.domain.SessionItemsEntity;
import com.worldtaletalk.inventory.dto.SessionItemsDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SessionItemsMapper {
    @Mapping(source = "storySession.id", target = "storySessionId")
    SessionItemsDTO toDTO(SessionItemsEntity sessionItemsEntity);
    List<SessionItemsDTO> toDTOList(List<SessionItemsEntity> sessionItemsEntity);
}
