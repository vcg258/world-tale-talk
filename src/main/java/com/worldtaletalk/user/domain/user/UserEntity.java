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
    private String loginId; // 로그인 아이디

    @Column(length = 50, unique = true, nullable = false)
    private String nickname; // 닉네임

    @Column(length = 70, unique = true, nullable = false)
    private String email; // 이메일

    @Column(length = 255) // null허용 이유 : 소셜로그인 할시 비밀번호 불필요
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


    /* 도메인 행위 메서드 */

    /**
     * 회원가입
     *
     * @param login_id 로그인 아이디
     * @param nickname 닉네임
     * @param email 이메일
     * @param password 비밀번호 (BCrypt)
     * @return 회원정보
     */
    public static UserEntity createUser(
            String login_id,
            String nickname,
            String email,
            String password
    ) {
        return UserEntity.builder()
                .loginId(login_id)
                .nickname(nickname)
                .email(email)
                .password(password)
                .build();
    }

    /**
     * 회원 정보 수정
     *
     * @param email 이메일
     * @param password 비밀번호
     */
    public void modifyInformationUser(
            String email,
            String password
    ) {
        this.email = email;
        this.password = password;
    }

    /**
     * 닉네임 변경
     * @param nickname 변경할 닉네임
     */
    public void changeNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * 회원 계정 상태 변경
     *
     * @param status 상태
     */
    public void changeStatus(UserStatus status) {
        this.status = status;
    }

    /**
     * 회원 삭제
     */
    public void userWithdrawal() {
        this.loginId = null;
        this.nickname = "del_" + this.id + "_" + this.nickname;
        this.email = "del_" + this.id + "_" + this.email;
        this.password = null;
        this.status = UserStatus.DELETED;
    }
}
