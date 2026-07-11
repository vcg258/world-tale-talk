package com.worldtaletalk.user.domain;

import com.worldtaletalk.user.domain.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Entity
@Builder
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "dormant_notifications")
public class DormantNotificationsEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id private Long id;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime notifiedAt = LocalDateTime.now(); // 휴면 계정 메일 발송 여부

    /* FK (자식) */
    @ToString.Exclude
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true, foreignKey = @ForeignKey(name = "fk_dormant_notifications_user"))
    private UserEntity user;
}
