package com.worldtaletalk.chat.domain;

import com.worldtaletalk.global.basetime.BaseTimeCreatedAtEntity;
import com.worldtaletalk.session.domain.storysessions.StorySessionsEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "chat_messages", indexes = {
        @Index(name = "idx_chat_messages_session_id", columnList = "session_id"),
        @Index(name = "idx_chat_messages_session_turn", columnList = "turn")
})
public class ChatMessagesEntity extends BaseTimeCreatedAtEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id private Long id;

    @Column(nullable = false)
    private int turn; // 해당 메세지가 속한 턴

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ChatRole role; // 채팅 대상

    @Column(length = Integer.MAX_VALUE, nullable = false)
    private String content; // 메시지 내용

    @Builder.Default
    @Column(nullable = false)
    private boolean isEpilogue = false; // 에필로그 구간 여부(true = 에필로그구간임)

    /* FK (자식) */
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false, foreignKey = @ForeignKey(name = "fk_chat_messages_session"))
    private StorySessionsEntity storySession;
}
