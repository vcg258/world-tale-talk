package com.worldtaletalk.chat.domain;

import com.worldtaletalk.global.basetime.BaseTimeEntity;
import com.worldtaletalk.session.domain.StorySessionsEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "chat_messages", indexes = {
        @Index(name = "idx_chat_messages_session_id", columnList = "session_id"),
        @Index(name = "idx_chat_messages_session_turn", columnList = "session_id, turn")
})
public class ChatMessagesEntity extends BaseTimeEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id private Long id;

    @Column(nullable = false)
    private int turn; // 해당 메세지가 속한 턴

    @Column(nullable = false, length = 15)
    @Enumerated(EnumType.STRING)
    private ChatRole role; // 발화 주체 (USER, ASSISTANT)

    @Column(length = Integer.MAX_VALUE, nullable = false)
    private String content; // 메시지 내용

    @Builder.Default
    @Column(name = "is_epilogue", nullable = false)
    private boolean epilogue = false; // 에필로그 구간 여부 (true = 에필로그 구간)

    /* FK (자식) */
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false, foreignKey = @ForeignKey(name = "fk_chat_messages_session"))
    private StorySessionsEntity storySession;

    /* 도메인 행위 메서드 */

    /**
     * 커스텀 ChatMemory 추가
     * @param turn 해당 메세지가 속한 턴
     * @param role 발화 주체
     * @param content 메세지 내용
     * @param epilogue 에필로그 여부
     * @param storySession 사용자 세션 FK
     * @return ChatMemory 정보
     */
    public static ChatMessagesEntity createChatMessagesEntity(
            int turn,
            ChatRole role,
            String content,
            boolean epilogue,
            StorySessionsEntity storySession
    ) {
        return ChatMessagesEntity.builder()
                .turn(turn)
                .role(role)
                .content(content)
                .epilogue(epilogue)
                .storySession(storySession)
                .build();
    }
}
