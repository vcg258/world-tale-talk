package com.worldtaletalk.item.domain.itemgradeprobabilities;

import com.worldtaletalk.global.basetime.BaseTimeEntity;
import com.worldtaletalk.item.domain.ItemGrade;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Check;

@Entity
@Getter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "item_grade_probabilities",
        uniqueConstraints = @UniqueConstraint(name = "item_grade_probabilities_phase_grade_key", columnNames = {"phase", "grade"}))
public class ItemGradeProbabilitiesEntity extends BaseTimeEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id private Long id;

    @Column(nullable = false, length = 15)
    @Enumerated(EnumType.STRING)
    private PhaseType phase; // 진행도 구간

    @Column(nullable = false, length = 15)
    @Enumerated(EnumType.STRING)
    private ItemGrade grade; // 아이템 등급

    @Check(constraints = "probability BETWEEN 0 AND 100")
    @Column(nullable = false)
    private int probability; // 아이템 등급 확률 (동일 구간 합산 100으로 정함)

    /* 도메인 행위 메서드 */

    /**
     * 아이템 등급 확률 수정 (관리자)
     * @param probability 확률 (0~100)
     */
    public void updateProbability(int probability) {
        if (probability < 0 || probability > 100) {
            throw new IllegalArgumentException("확률은 0에서 100 사이여야 합니다.");
        }
        this.probability = probability;
    }
}
