package com.worldtaletalk.report.domain;

import com.worldtaletalk.global.basetime.BaseTimeEntity;
import com.worldtaletalk.user.domain.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "reports", indexes = {
        @Index(name = "idx_reports_status", columnList = "status"),
        @Index(name = "idx_reports_reporter_id", columnList = "reporter_id"),
        @Index(name = "idx_reports_handled_by", columnList = "handled_by")
})
public class ReportsEntity extends BaseTimeEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id private Long id;

    @Column(nullable = false, length = 15)
    @Enumerated(EnumType.STRING)
    private ReportTargetType targetType; // 신고 대상 종류 (REVIEW, CHAT_MESSAGE)

    @Column(nullable = false)
    private Long targetId; // 신고 대상 아이디

    @Column(length = Integer.MAX_VALUE, nullable = false)
    private String reason; // 신고 사유

    @Builder.Default
    @Column(nullable = false, length = 15)
    @Enumerated(EnumType.STRING)
    private ReportStatus status = ReportStatus.PENDING; // 처리 상태 (PENDING, RESOLVED, DISMISSED)

    private LocalDateTime handledAt; // 처리 일시 (처리전 null)

    /* FK (자식) */
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false, foreignKey = @ForeignKey(name = "fk_reports_reporter"))
    private UserEntity users; // 신고한 유저

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "handled_by", foreignKey = @ForeignKey(name = "fk_reports_handled_by"))
    private UserEntity admin; // 처리할 관리자 (처리전 null)


    /* 도메인 행위 메서드 */

    /**
     * 신고 등록
     *
     * @param reportTargetType 신고 출처 (리뷰, 채팅 내역
     * @param targetId 신고한 유저 아이디
     * @param reason 신고 사유
     * @param users 일반 사용자
     * @return 신고 등록
     */
    public static ReportsEntity createReport(
            ReportTargetType reportTargetType,
            long targetId,
            String reason,
            UserEntity users
    ) {
        return ReportsEntity.builder()
                .targetType(reportTargetType)
                .targetId(targetId)
                .reason(reason)
                .users(users)
                .build();
    }

    /**
     * 신고 내역 처리
     *
     * @param reportStatus 신고 내역 처리 상태
     * @param admin 관리자
     */
    public void modifyInformationReport(
            ReportStatus reportStatus,
            UserEntity admin
    ) {
        this.status = reportStatus;
        this.handledAt = LocalDateTime.now();
        this.admin = admin;
    }
}
