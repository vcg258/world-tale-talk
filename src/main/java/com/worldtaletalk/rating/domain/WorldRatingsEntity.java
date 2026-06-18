package com.worldtaletalk.rating.domain;

import com.worldtaletalk.global.basetime.BaseTimeEntity;
import com.worldtaletalk.session.domain.StorySessionsEntity;
import com.worldtaletalk.user.domain.user.UserEntity;
import com.worldtaletalk.world.domain.WorldsEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Check;

@Entity
@Getter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "world_ratings",
        indexes = {
        @Index(name = "idx_world_ratings_user_id", columnList = "user_id"),
        @Index(name = "idx_world_ratings_world_id", columnList = "world_id")
})
public class WorldRatingsEntity extends BaseTimeEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id private Long id;

    @Column(nullable = false)
    @Check(constraints = "score BETWEEN 1 AND 5")
    private short score; // 별점 (1 ~ 5점)

    @Column(length = 100)
    private String review; // 한줄 리뷰 (선택)

    @Builder.Default
    @Column(length = 4, nullable = false)
    private int countPlayNumber = 0; // 해당 세게관 클리어한 수 (몇번 플레이를 했다라는걸 명시 하기 위함)

    /* FK (자식) */
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_world_ratings_user"))
    private UserEntity users;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "world_id", nullable = false, foreignKey = @ForeignKey(name = "fk_world_ratings_world"))
    private WorldsEntity world;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false, unique = true, foreignKey = @ForeignKey(name = "fk_world_ratings_session"))
    private StorySessionsEntity storySession;

    /* 도메인 행위 메서드 */

    /**
     * 세계관 별점 및 리뷰 생성
     * @param score 별점
     * @param review 리뷰
     * @param countPlayNumber 해당 세계관 클리어 횟수
     * @param user 유저 정보
     * @param world 세계관 정보
     * @param storySession 세션 정보
     * @return 별점 및 리뷰
     */
    public static WorldRatingsEntity createWorldRating(
        short score,
        String review,
        int countPlayNumber,
        UserEntity user,
        WorldsEntity world,
        StorySessionsEntity storySession
    ) {
        return WorldRatingsEntity.builder()
                .score(score)
                .review(review)
                .countPlayNumber(countPlayNumber)
                .users(user)
                .world(world)
                .storySession(storySession)
                .build();
    }

    /**
     * 세계관 별점 및 리뷰 수정
     * @param score 별점
     * @param review 리뷰
     */
    public void modifyInformationWorldRating(short score, String review) {
        this.score = score;
        this.review = review;
    }
}
