package com.worldtaletalk.stat.domain;

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
@Table(name = "session_stat_logs", indexes = {
        @Index(name = "idx_session_stat_logs_session_id", columnList = "session_id")
})
public class SessionStatLogsEntity extends BaseTimeEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id private Long id;

    @Column(nullable = false)
    private int turn; // 스텟 변화가 발생한 턴

    @Builder.Default
    @Column(nullable = false)
    private int wisdomDelta = 0; // 지혜 변화량

    @Builder.Default
    @Column(nullable = false)
    private int strengthDelta = 0; // 힘 변화량

    @Builder.Default
    @Column(nullable = false)
    private int agilityDelta = 0; // 민첩 변화량

    @Builder.Default
    @Column(nullable = false)
    private int charismaDelta = 0; // 카리스마 변화량

    @Builder.Default
    @Column(nullable = false)
    private int luckDelta = 0; // 행운 변화량

    /* FK (자식) */
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false, foreignKey = @ForeignKey(name = "fk_session_stat_logs_session"))
    private StorySessionsEntity storySession;
}
