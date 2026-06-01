package com.worldtaletalk.chat.dto;

import com.worldtaletalk.chat.domain.ChatRole;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessagesDTO{
    private Long id;
    private Long storySessionId; // 플레이 세션 FK
    private int turn; // 해당 메세지가 속한 턴
    private ChatRole role; // 발화 주체
    private String content; // 메시지 내용
    private boolean epilogue; // 에필로그 구간 여부(true = 에필로그구간임)
}
