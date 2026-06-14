package com.worldtaletalk.chat;

import com.worldtaletalk.chat.domain.ChatMessagesEntity;
import com.worldtaletalk.chat.dto.ChatMessagesDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;


// TODO 얘는 필요한가? 사용안하면 폐기 예정
@Mapper(componentModel = "spring")
public interface ChatMessagesMapper {
    @Mapping(source = "storySession.id", target = "storySessionId")
    ChatMessagesDTO toDTO(ChatMessagesEntity chatMessagesEntity);
    List<ChatMessagesDTO> toDTOList(List<ChatMessagesEntity> chatMessagesEntityList);
}
