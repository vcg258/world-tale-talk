package com.worldtaletalk.notice.domain;

import com.worldtaletalk.global.basetime.BaseTimeEntity;
import com.worldtaletalk.user.domain.users.UsersEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "notices",
        indexes = {
        @Index(name = "idx_notices_is_active", columnList = "is_active"),
        @Index(name = "idx_notices_admin_id", columnList = "admin_id")
})
public class NoticesEntity extends BaseTimeEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id private Long id;

    @Column(length = 50, nullable = false)
    private String title; // 공지 제목

    @Column(length = Integer.MAX_VALUE, nullable = false)
    private String content; // 공지 본문

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean active = false; // 공지 공개 / 비공개 여부 (false = 비공개)

    /* FK (자식) */
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false, foreignKey = @ForeignKey(name = "fk_notices_admin"))
    private UsersEntity admins;
}
