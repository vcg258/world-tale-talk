package com.worldtaletalk.user.domain.user;

import com.worldtaletalk.global.basetime.BaseTimeEntity;
import com.worldtaletalk.notice.domain.NoticesEntity;
import com.worldtaletalk.rating.domain.WorldRatingsEntity;
import com.worldtaletalk.report.domain.ReportsEntity;
import com.worldtaletalk.session.domain.StorySessionsEntity;
import com.worldtaletalk.user.domain.UserSuspensionsEntity;
import com.worldtaletalk.user.domain.socialaccounts.SocialAccountsEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class UserEntity extends BaseTimeEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id private Long id; // PK

    @Column(length = 30, unique = true) // null허용 이유 : 소셜로그인 할시 아이디 불필요
    private String login_id; // 로그인 아이디

    @Column(length = 20, unique = true, nullable = false)
    private String nickname; // 닉네임

    @Column(length = 50, unique = true, nullable = false)
    private String email; // 이메일

    @Column(length = 60) // null허용 이유 : 소셜로그인 할시 비밀번호 불필요
    private String password; // 비밀번호

    @Builder.Default
    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.USER; // 권한 (USER, ADMIN)

    @Builder.Default
    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private UserStatus status = UserStatus.ACTIVE; // 계정 상태 (ACTIVE, SUSPENDED, DELETED)


    /* FK (부모) */
    @ToString.Exclude
    @OneToMany(mappedBy = "users")
    private List<SocialAccountsEntity> socialAccounts;

    @ToString.Exclude
    @OneToMany(mappedBy = "users")
    private List<UserSuspensionsEntity> suspensionsEntityUser;

    @ToString.Exclude
    @OneToMany(mappedBy = "admin")
    private List<UserSuspensionsEntity> suspensionsEntityAdmin;

    @ToString.Exclude
    @OneToMany(mappedBy = "users")
    private List<StorySessionsEntity> storySessions;

    @ToString.Exclude
    @OneToMany(mappedBy = "users")
    private List<WorldRatingsEntity> worldRatings;

    @ToString.Exclude
    @OneToMany(mappedBy = "users")
    private List<ReportsEntity> reporterUser;

    @ToString.Exclude
    @OneToMany(mappedBy = "admin")
    private List<ReportsEntity> reporterAdmin;

    @ToString.Exclude
    @OneToMany(mappedBy = "admin")
    private List<NoticesEntity> notices;
}
