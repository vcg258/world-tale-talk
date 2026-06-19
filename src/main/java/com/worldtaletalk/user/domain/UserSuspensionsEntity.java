package com.worldtaletalk.user.domain;

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
@Table(name = "user_suspensions", indexes = {
        @Index(name = "idx_user_suspensions_user_id", columnList = "user_id"),
        @Index(name = "idx_user_suspensions_admin_id", columnList = "admin_id")
})
public class UserSuspensionsEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id private Long id;

    @Column(length = Integer.MAX_VALUE, nullable = false)
    private String reason; // 정지 사유

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime suspendedAt = LocalDateTime.now(); // 정지 처리 일시

    private LocalDateTime liftedAt; // 정지 해제 일시 (NULL 이면 정지 중)

    /* FK (자식) */
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_user_suspensions_user"))
    private UserEntity users;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false, foreignKey = @ForeignKey(name = "fk_user_suspensions_admin"))
    private UserEntity admin;


    /* 도메인 행위 메서드 */

    /**
     * 정지 이력 생성
     *
     * @param user 회원
     * @param admin 관리자
     * @param reason 정지 사유
     * @return 정지 이력
     */
    public static UserSuspensionsEntity createUserSuspensionsEntity(
            UserEntity user,
            UserEntity admin,
            String reason
    ) {
        return UserSuspensionsEntity.builder()
                .users(user)
                .admin(admin)
                .reason(reason)
                .build();
    }

    /**
     * 정지 해제 일시 업데이트
     */
    public void modifyUnstoppableTime(
    ) {
        this.liftedAt = LocalDateTime.now();
    }

}
