package com.worldtaletalk.user.domain.socialaccounts;

import com.worldtaletalk.global.basetime.BaseTimeEntity;
import com.worldtaletalk.user.domain.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "social_accounts",
        indexes = @Index(name = "idx_social_accounts_user_id", columnList = "user_id"),
        uniqueConstraints = @UniqueConstraint(columnNames = {"provider", "provider_id"}))
public class SocialAccountsEntity extends BaseTimeEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id private Long id;

    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private SocialProvider provider; // 소셜 로그인 제공사 (GOOGLE, KAKAO, NAVER)

    @Column(length = 100, nullable = false)
    private String providerId; // 소셜 제공사 고유번호

    /* FK (자식) */
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_social_accounts_user"))
    private UserEntity users; // 회원 PK


    /* 도메인 행위 메서드 */

    /**
     * 소셜 로그인 연동 정보 생성
     * @param socialProvider 소셜 로그인 제공사
     * @param providerId 소셜 제공사 고유번호
     * @param user 회원 정보
     * @return 소셜 로그인 연동 정보
     */
    public static SocialAccountsEntity createSocialAccount (
            SocialProvider socialProvider,
            String providerId,
            UserEntity user
    ) {
        return SocialAccountsEntity.builder()
                .provider(socialProvider)
                .providerId(providerId)
                .users(user)
                .build();
    }
}
