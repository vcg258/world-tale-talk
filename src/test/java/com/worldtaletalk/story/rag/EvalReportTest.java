package com.worldtaletalk.story.rag;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EvalReportTest {

    @Test
    @DisplayName("유형별과 전체(ALL) 평균이 맞게 계산되고 줄 순서는 처음 나온 유형 순서 마지막이 ALL이며 빈 입력은 예외")
    void averageByTypeTest() {
        List<CaseResult> results = createResults();
        List<TypeSummary> summaries = EvalReport.averageByType(results);

        assertAll(
                () -> assertEquals(3, summaries.size()),
                // name값과 순서 검증
                () -> assertEquals("DIRECT", summaries.getFirst().name()),
                () -> assertEquals("ACTION", summaries.get(1).name()),
                () -> assertEquals("ALL", summaries.getLast().name()),
                // queryCount 검증
                () -> assertEquals(2, summaries.getFirst().queryCount()),
                () -> assertEquals(1, summaries.get(1).queryCount()),
                () -> assertEquals(3, summaries.getLast().queryCount()),
                // recall1 평균 검증
                () -> assertEquals(0.5, summaries.getFirst().recallAvg1()),
                () -> assertEquals(0.0, summaries.get(1).recallAvg1()),
                () -> assertEquals(1.0 / 3, summaries.getLast().recallAvg1(), 1e-9),
                // recall3 평균 검증
                () -> assertEquals(1.0, summaries.getFirst().recallAvg3()),
                () -> assertEquals(0.0, summaries.get(1).recallAvg3()),
                () -> assertEquals(2.0 / 3, summaries.getLast().recallAvg3(), 1e-9),
                // recall5 평균 검증
                () -> assertEquals(1.0, summaries.getFirst().recallAvg5()),
                () -> assertEquals(1.0, summaries.get(1).recallAvg5()),
                () -> assertEquals(1.0, summaries.getLast().recallAvg5()),
                // mrr 평균 검증
                () -> assertEquals(0.75, summaries.getFirst().mrrAvg5()),
                () -> assertEquals(0.25, summaries.get(1).mrrAvg5()),
                () -> assertEquals(1.75 / 3, summaries.getLast().mrrAvg5(), 1e-9),
                // 빈값은 예외처리
                () -> assertThrows(IllegalArgumentException.class, () -> EvalReport.averageByType(List.of()))
        );
    }

    // 테스트용 더미 값 추가 헬퍼
    private static List<CaseResult> createResults() {
        List<CaseResult> results = new ArrayList<>();
        CaseResult caseResult1 = new CaseResult(
                "a",
                "DIRECT",
                1,
                1.0,
                1.0,
                1.0,
                1.0
        );
        CaseResult caseResult2 = new CaseResult(
                "b",
                "DIRECT",
                2,
                0.0,
                1.0,
                1.0,
                0.5
        );
        CaseResult caseResult3 = new CaseResult(
                "c",
                "ACTION",
                4,
                0.0,
                0.0,
                1.0,
                0.25
        );
        results.add(caseResult1);
        results.add(caseResult2);
        results.add(caseResult3);
        return results;
    }
}
