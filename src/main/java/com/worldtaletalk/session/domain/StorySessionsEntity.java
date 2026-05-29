package com.worldtaletalk.session.domain;

import com.worldtaletalk.chat.domain.ChatMessagesEntity;
import com.worldtaletalk.global.basetime.BaseTimeEntity;
import com.worldtaletalk.inventory.domain.SessionItemsEntity;
import com.worldtaletalk.persona.domain.PersonasEntity;
import com.worldtaletalk.rating.domain.WorldRatingsEntity;
import com.worldtaletalk.stat.domain.SessionStatLogsEntity;
import com.worldtaletalk.stat.domain.SessionStatSnapshotsEntity;
import com.worldtaletalk.user.domain.users.UsersEntity;
import com.worldtaletalk.world.domain.WorldsEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "story_sessions",
        indexes = {
        @Index(name = "idx_story_sessions_user_id", columnList = "user_id"),
        @Index(name = "idx_story_sessions_world_id", columnList = "world_id"),
        @Index(name = "idx_story_sessions_persona_id", columnList = "persona_id"),
        @Index(name = "idx_story_sessions_status", columnList = "status")
})
public class StorySessionsEntity extends BaseTimeEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id private Long id;

    @Builder.Default
    @Column(nullable = false, length = 15)
    @Enumerated(EnumType.STRING)
    private SessionStatus status = SessionStatus.IN_PROGRESS; // 세션 상태 (IN_PROGRESS, ENDING, EPILOGUE, COMPLETED, ABANDONED)

    @Builder.Default
    @Column(nullable = false)
    private int currentTurn = 0; // 현재 턴 수

    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private EndingType endingType; // 도달한 엔딩 유형 (null = 아직 엔딩 아님)

    /* FK (부모) */
    @ToString.Exclude
    @OneToMany(mappedBy = "storySession")
    private List<SessionStatLogsEntity> sessionStatLogEntities;

    @ToString.Exclude
    @OneToMany(mappedBy = "storySession")
    private List<ChatMessagesEntity> chatMessagesEntities;

    @ToString.Exclude
    @OneToMany(mappedBy = "storySession")
    private List<SessionItemsEntity> sessionItemsEntities;

    @ToString.Exclude
    @OneToMany(mappedBy = "storySession")
    private List<WorldRatingsEntity> worldRatings;

    @ToString.Exclude
    @OneToOne(mappedBy = "storySession")
    private SessionStatSnapshotsEntity sessionStatSnapshotsEntities;

    /* FK (자식) */
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_story_sessions_user"))
    private UsersEntity users;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "world_id", nullable = false, foreignKey = @ForeignKey(name = "fk_story_sessions_world"))
    private WorldsEntity worlds;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "persona_id", nullable = false, foreignKey = @ForeignKey(name = "fk_story_sessions_persona"))
    private PersonasEntity personas;
}
