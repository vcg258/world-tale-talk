package com.worldtaletalk.report.domain;

import com.worldtaletalk.global.basetime.BaseTimeCreatedAtEntity;
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
@Table(name = "reports")
public class ReportsEntity extends BaseTimeCreatedAtEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReportTargetType targetType; // 신고 대상 종류

    @Column(nullable = false)
    private Long targetId; // 신고 대상 아이디

    @Column(length = Integer.MAX_VALUE, nullable = false)
    private String reason; // 신고 사유

    @Builder.Default
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReportStatus status = ReportStatus.PENDING; // 처리 상태

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
