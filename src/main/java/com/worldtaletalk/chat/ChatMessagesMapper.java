package com.worldtaletalk.chat;

import com.worldtaletalk.config.MapStructConfig;
import com.worldtaletalk.chat.domain.ChatMessagesEntity;
import com.worldtaletalk.chat.dto.ChatMessagesDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;


@Mapper(config = MapStructConfig.class)
public interface ChatMessagesMapper {
    @Mapping(source = "storySession.id", target = "storySessionId")
    ChatMessagesDTO toDTO(ChatMessagesEntity chatMessagesEntity);
    List<ChatMessagesDTO> toDTOList(List<ChatMessagesEntity> chatMessagesEntityList);
}
