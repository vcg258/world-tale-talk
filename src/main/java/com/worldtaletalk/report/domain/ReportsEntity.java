package com.worldtaletalk.report.domain;

import com.worldtaletalk.global.basetime.BaseTimeEntity;
import com.worldtaletalk.user.domain.users.UsersEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
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
    private ReportTargetType targetType;

    @Column(nullable = false)
    private Long targetId;

    @Column(length = Integer.MAX_VALUE, nullable = false)
    private String reason;

    @Builder.Default
    @Column(nullable = false, length = 15)
    @Enumerated(EnumType.STRING)
    private ReportStatus status = ReportStatus.PENDING;

    private LocalDateTime handledAt; // 처리 일시 (처리전 null)

    /* FK (자식) */
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false, foreignKey = @ForeignKey(name = "fk_reports_reporter"))
    private UsersEntity users; // 신고한 유저

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "handled_by", foreignKey = @ForeignKey(name = "fk_reports_handled_by"))
    private UsersEntity admins; // 처리할 관리자 (처리전 null)
}
