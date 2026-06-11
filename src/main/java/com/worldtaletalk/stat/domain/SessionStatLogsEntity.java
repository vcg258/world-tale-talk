package com.worldtaletalk.stat.domain;

import com.worldtaletalk.global.basetime.BaseTimeEntity;
import com.worldtaletalk.session.domain.StorySessionsEntity;
import com.worldtaletalk.stat.dto.SessionStatLogsDTO;
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

    /* 도메인 행위 메서드 */

    /**
     * 스텟 로그 생성
     * @param turn 스텟 변화가 발생한 턴
     * @param wisdomDelta 지혜
     * @param strengthDelta 힘
     * @param agilityDelta 민첩
     * @param charismaDelta 카리스마
     * @param luckDelta 행운
     * @param storySession 세션 FK
     * @return 스텟 로그
     */
    public static SessionStatLogsEntity createSessionStatLog(
            int turn,
            int wisdomDelta,
            int strengthDelta,
            int agilityDelta,
            int charismaDelta,
            int luckDelta,
            StorySessionsEntity storySession
    ) {
        return SessionStatLogsEntity.builder()
                .turn(turn)
                .wisdomDelta(wisdomDelta)
                .strengthDelta(strengthDelta)
                .agilityDelta(agilityDelta)
                .charismaDelta(charismaDelta)
                .luckDelta(luckDelta)
                .storySession(storySession)
                .build();
    }
}
