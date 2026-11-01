package com.worldtaletalk.story.rag;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RandomBaselineTest {
    // 테스트를 위한 더미 데이터
    private final List<String> chunks = List.of("alpha", "bravo", "charlie", "delta", "echo", "foxtrot");
    private final RagEvalCase single = new RagEvalCase("t-1", "test", "SINGLE", "질문", List.of("alpha"));
    private final RagEvalCase multi  = new RagEvalCase("t-2", "test", "MULTI",  "질문", List.of("alpha", "bravo"));

    @Test
    @DisplayName("같은 시드면 결과는 동일함")
    void randomBaselineTestIsOne() {
        List<TypeSummary> result = RandomBaseline.simulate(List.of(single), chunks, 10000, 42L);
        List<TypeSummary> result2 = RandomBaseline.simulate(List.of(single), chunks, 10000, 42L);

        assertEquals(result, result2);
    }

    @Test
    @DisplayName("정답 1개, 청크 6개일 때 무작위 점수가 이론값에 가까움")
    void randomBaselineTestIsTwo() {
        TypeSummary all = RandomBaseline.simulate(List.of(single), chunks, 10000, 42L).getLast();

        assertAll(
                () -> assertEquals(1.0 / 6, all.recallAvg1(), 0.02),
                () -> assertEquals(3.0 / 6, all.recallAvg3(), 0.02),
                () -> assertEquals(5.0 / 6, all.recallAvg5(), 0.02),
                // (1 + 1/2 + 1/3 + 1/4 + 1/5) / 6 = 0.3806
                () -> assertEquals(0.3806, all.mrrAvg5(), 0.02)
        );
    }

    @Test
    @DisplayName("정답이 2개이면 1개일 때보다 MRR이 높음")
    void randomBaselineTestIsThree() {
        double singleMrr = RandomBaseline.simulate(List.of(single), chunks, 10000, 42L).getLast().mrrAvg5();
        double multiMrr = RandomBaseline.simulate(List.of(multi), chunks, 10000, 42L).getLast().mrrAvg5();

        assertTrue(multiMrr > singleMrr);
        // (첫 정답이 k번째인 경우의 수 / 전체 경우의 수 15) × (그때의 점수 1/k)
        // 5/15*1 + 4/15*1/2 + 3/15*1/3 + 2/15*1/4 + 1/15*1/5
        assertEquals(0.58, multiMrr, 0.02);
    }

    @Test
    @DisplayName("trials가 0이거나 청크가 비었을 경우 예외를 발생시킨다.")
    void randomBaselineTestIsFour() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> RandomBaseline.simulate(List.of(single), chunks, 0, 42L)),
                () -> assertThrows(IllegalArgumentException.class, () -> RandomBaseline.simulate(List.of(single), List.of(), 10000, 42L))
        );
    }
}