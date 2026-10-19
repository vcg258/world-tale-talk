package com.worldtaletalk.story.rag;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class RetrievalMetricsTest {
    @Test
    @DisplayName("정답이 1번째에 있는 경우 Rank = 1")
    void firstRelevantRankTestIsOne() {
        List<String> retrieved = List.of("무관한 청크", "나는 왕이다.", "내이름은 김철수이고 직업은 백수");
        int rank = RetrievalMetrics.firstRelevantRank(retrieved, List.of("무관한 청크"));
        log.info("Rank: {}", rank);

        assertEquals(1, rank);
    }

    @Test
    @DisplayName("정답이 2번째에 있는 경우 Rank = 2")
    void firstRelevantRankTestIsTwo() {
        List<String> retrieved = List.of("무관한 청크", "나는 왕이다.", "내이름은 김철수이고 직업은 백수");
        int rank = RetrievalMetrics.firstRelevantRank(retrieved, List.of("왕이다."));

        assertEquals(2, rank);
    }

    @Test
    @DisplayName("정답이 3번째에 있는 경우 Rank = 3")
    void firstRelevantRankTestIsThree() {
        List<String> retrieved = List.of("무관한 청크", "나는 왕이다.", "내이름은 김철수이고 직업은 백수");
        int rank = RetrievalMetrics.firstRelevantRank(retrieved, List.of("내이름은 김철수이고 직업은 백수"));

        assertEquals(3, rank);
    }

    @Test
    @DisplayName("정답이 없는 경우 Rank = 0")
    void firstRelevantRankTestIsFour() {
        List<String> retrieved = List.of("무관한 청크", "나는 왕이다.", "내이름은 김철수이고 직업은 백수");
        int rank = RetrievalMetrics.firstRelevantRank(retrieved, List.of("123"));

        assertEquals(0, rank);
    }

    @Test
    @DisplayName("상위 1개 안에 정답 문구 비율 atk = 0.0")
    void recallAtKTestIsOne() {
        List<String> retrieved = List.of("무관한 청크", "나는 왕이다.", "내이름은 김철수이고 직업은 백수");
        double atK = RetrievalMetrics.recallAtK(retrieved, List.of("내이름은 김철수", "왕"), 1);

        assertEquals(0.0, atK);
    }

    @Test
    @DisplayName("상위 2개 안에 정답 문구 비율 atk = 0.5")
    void recallAtKTestIsTwo() {
        List<String> retrieved = List.of("무관한 청크", "나는 왕이다.", "내이름은 김철수이고 직업은 백수");
        double atK = RetrievalMetrics.recallAtK(retrieved, List.of("내이름은 김철수", "왕"), 2);

        assertEquals(0.5, atK);
    }

    @Test
    @DisplayName("상위 3개 안에 정답 문구 비율 atk = 1.0")
    void recallAtKTestIsThree() {
        List<String> retrieved = List.of("무관한 청크", "나는 왕이다.", "내이름은 김철수이고 직업은 백수");
        double atK = RetrievalMetrics.recallAtK(retrieved, List.of("내이름은 김철수", "왕"), 3);
        log.info("AtK: {}", atK);

        assertEquals(1.0, atK);
    }

    @Test
    @DisplayName("상위 0개 안에 정답 문구 비율이 없는 경우(즉 k범위에 없으면 정답을 찾지 못함) atk = 0.0")
    void recallAtKTestIsFour() {
        List<String> retrieved = List.of("무관한 청크", "나는 왕이다.", "내이름은 김철수이고 직업은 백수");
        double atK = RetrievalMetrics.recallAtK(retrieved, List.of("내이름은 김철수", "왕"), 0);

        assertEquals(0.0, atK);
    }

    @Test
    @DisplayName("anchor에 빈값일 경우 나눌 수 없으므로 예외")
    void recallAtKTestIsFive() {
        List<String> retrieved = List.of("무관한 청크", "나는 왕이다.", "내이름은 김철수이고 직업은 백수");
        assertThrows(IllegalArgumentException.class, () -> RetrievalMetrics.recallAtK(retrieved, List.of(""), 3));
    }

    @Test
    @DisplayName("retrieved에 빈값일 경우 0.0")
    void recallAtKTestIsSix() {
        List<String> retrieved = List.of("");
        double atK = RetrievalMetrics.recallAtK(retrieved, List.of("내이름은 김철수", "왕"), 3);

        assertEquals(0.0, atK);
    }

    @Test
    @DisplayName("순위가 2이면 역수는 0.5")
    void reciprocalRankTestIsOne() {
        assertEquals(0.5, RetrievalMetrics.reciprocalRank(2), 1e-9); // 0.333... 이런식으로 나올 수 있으니 오차 허용값 적용
    }

    @Test
    @DisplayName("순위가 0(못 찾음)이면 역수는 0.0")
    void reciprocalRankTestIsTwo() {
        assertEquals(0.0, RetrievalMetrics.reciprocalRank(0), 1e-9);
    }

    @Test
    @DisplayName("청크에 줄바꿈이 섞여 있어도 정답 문구가 매칭된다")
    void reciprocalRankTestIsThree() {
        List<String> retrieved = List.of("무관한 청크", "나는 왕\n이다.");
        assertEquals(2, RetrievalMetrics.firstRelevantRank(retrieved, List.of("왕 이다.")));
    }

    @Test
    @DisplayName("contains()에 빈값이나 공백이 들어있는 경우 예외")
    void containsTestIsOne() {
        assertThrows(IllegalArgumentException.class, () -> RetrievalMetrics.contains("무관한 청크", ""));
    }
}
