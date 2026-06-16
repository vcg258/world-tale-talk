package com.worldtaletalk.notice.domain;

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
    private UserEntity admin;

    /* 도메인 행위 메서드 */

    /**
     * 공지글 생성
     * @param admin 관리자 PK
     * @param title 공지글 제목
     * @param content 공지글 내용
     * @param active 공지글 활성화 여부
     * @return 공지글
     */
    public static NoticesEntity createNotices(
            UserEntity admin,
            String title,
            String content,
            boolean active
    ) {
        if (admin == null) {
            throw new IllegalArgumentException("관리자 FK 필수 (이거 뜨면 로직이 문제 있음)");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("제목은 필수 값입니다.");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("내용은 필수 값입니다.");
        }

        return NoticesEntity.builder()
                .admin(admin)
                .title(title)
                .content(content)
                .active(active)
                .build();
    }

    public void modifyInformation(
            String title,
            String content,
            boolean active
    ) {
        this.title = title;
        this.content = content;
        this.active = active;
    }


}
