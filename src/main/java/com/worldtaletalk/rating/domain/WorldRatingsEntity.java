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
@Table(name = "world_ratings", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "world_id"}),
        indexes = {
        @Index(name = "idx_world_ratings_user_id", columnList = "user_id"),
        @Index(name = "idx_world_ratings_world_id", columnList = "world_id"),
        @Index(name = "idx_world_ratings_session_id", columnList = "session_id")
})
public class WorldRatingsEntity extends BaseTimeEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id private Long id;

    @Column(nullable = false)
    @Check(constraints = "score BETWEEN 1 AND 5")
    private short score; // 별점 (1 ~ 5점)

    @Column(length = 100)
    private String review; // 한줄 리뷰 (선택)

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
    @JoinColumn(name = "session_id", nullable = false, foreignKey = @ForeignKey(name = "fk_world_ratings_session"))
    private StorySessionsEntity storySession;
}
