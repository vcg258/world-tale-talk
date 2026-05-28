package com.worldtaletalk.session.domain;

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
@Table(name = "session_stat_logs", indexes = {
        @Index(name = "idx_session_stat_logs_session_id", columnList = "session_id")
})
public class SessionStatLogsEntity extends BaseTimeCreatedAtEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id private Long id;

    @Column(nullable = false)
    private int turn; // 스텟 변화가 발생한 턴

    @Builder.Default
    @Column(nullable = false)
    private int wisdom_delta = 0; // 지혜 변화량

    @Builder.Default
    @Column(nullable = false)
    private int strength_delta = 0; // 힘 변화량

    @Builder.Default
    @Column(nullable = false)
    private int agility_delta = 0; // 민첩 변화량

    @Builder.Default
    @Column(nullable = false)
    private int charisma_delta = 0; // 카리스마 변화량

    @Builder.Default
    @Column(nullable = false)
    private int luck_delta = 0; // 행운 변화량

    /* FK (자식) */
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false, foreignKey = @ForeignKey(name = "fk_session_stat_logs_session"))
    private StorySessionsEntity storySession;
}
