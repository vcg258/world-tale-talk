package com.worldtaletalk.story.rag;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class EvalSetValidatorTest {
    // 청크 4개: 마지막 두 개는 "서약단원은 오십 명이다"가 두 청크에 걸쳐 쪼개진 모양
    private static final List<String> CHUNKS = List.of(
            "마정석 한 개는 금화 두 닢에 거래된다. 마법은 일곱 등급으로 나뉜다.",
            "은빛 의회의 의장은 엘다린이다. 서약단장은 세이론이며 의회에 비판적이다.",
            "서약단원은 오십",
            "명이다. 호수 접근은 금지되어 있다."
    );

    // 정답 문구가 청크 하나 안에 온전히 있는 질문 (정상)
    private static final RagEvalCase OK_SINGLE = new RagEvalCase("ok-1", "test", "TEST", "질문",
            List.of("마정석 한 개는 금화 두 닢에 거래된다"));

    // 정답 문구가 둘이고 모두 있는 질문 (정상)
    private static final RagEvalCase OK_DOUBLE = new RagEvalCase("ok-2", "test", "TEST", "질문",
            List.of("은빛 의회의 의장은 엘다린이다", "서약단장은 세이론이며"));

    // 정답 문구가 하나인데 어떤 청크에도 없음 (깨짐)
    private static final RagEvalCase BROKEN_SINGLE = new RagEvalCase("broken-1", "test", "TEST", "질문",
            List.of("존재하지 않는 문구"));

    // 정답 문구가 둘 다 없음 (깨짐, id가 한 번만 나와야 함)
    private static final RagEvalCase BROKEN_BOTH = new RagEvalCase("broken-2", "test", "TEST", "질문",
            List.of("없는 문구 하나", "없는 문구 둘"));

    // 정답 문구 둘 중 하나만 없음 (깨짐)
    private static final RagEvalCase BROKEN_HALF = new RagEvalCase("half-1", "test", "TEST", "질문",
            List.of("마법은 일곱 등급으로 나뉜다", "없는 문구"));

    // 정답 문구가 두 청크에 걸쳐 쪼개짐 (깨짐, 청크 경계 시나리오)
    private static final RagEvalCase SPLIT_ACROSS_CHUNKS = new RagEvalCase("split-1", "test", "TEST", "질문",
            List.of("서약단원은 오십 명이다"));

    // 질문 4개 (commonCases는 id만 보므로 정답 문구는 아무 값이나)
    private static final RagEvalCase Q1 = new RagEvalCase("q1", "test", "TEST", "질문", List.of("정답 문구 1"));
    private static final RagEvalCase Q2 = new RagEvalCase("q2", "test", "TEST", "질문", List.of("정답 문구 2"));
    private static final RagEvalCase Q3 = new RagEvalCase("q3", "test", "TEST", "질문", List.of("정답 문구 3"));
    private static final RagEvalCase Q4 = new RagEvalCase("q4", "test", "TEST", "질문", List.of("정답 문구 4"));
    private static final List<RagEvalCase> QUESTIONS = List.of(Q1, Q2, Q3, Q4);

    // 크기마다 findBrokenCases가 돌려준 결과라고 가정한 Map
    private static final Map<String, List<String>> BROKEN_AT_800 = Map.of();   // 깨진 질문 없음
    private static final Map<String, List<String>> BROKEN_AT_400 = Map.of(
            "q2", List.of("정답 문구 2"));
    // 400과 겹치지 않는 깨짐 (크기마다 깨지는 질문이 다를 수 있음을 표현)
    private static final Map<String, List<String>> BROKEN_AT_300 = Map.of(
            "q3", List.of("정답 문구 3"));
    private static final Map<String, List<String>> BROKEN_AT_200 = Map.of(
            "q2", List.of("정답 문구 2"),
            "q4", List.of("정답 문구 4"));
    private static final Map<String, List<String>> BROKEN_ALL = Map.of(
            "q1", List.of("정답 문구 1"),
            "q2", List.of("정답 문구 2"),
            "q3", List.of("정답 문구 3"),
            "q4", List.of("정답 문구 4"));

    @Test
    @DisplayName("모든 정답 문구가 청크에 있으면 빈 Map")
    void findBrokenCasesTestIsOne() {
        Map<String, List<String>> result1 = EvalSetValidator.findBrokenCases(List.of(OK_SINGLE), CHUNKS);
        Map<String, List<String>> result2 = EvalSetValidator.findBrokenCases(List.of(OK_DOUBLE), CHUNKS);

        assertAll(
                () -> assertTrue(result1.isEmpty()),
                () -> assertTrue(result2.isEmpty())
        );
    }

    @Test
    @DisplayName("정답 문구 하나인 질문이 깨짐")
    void findBrokenCasesTestIsTwo() {
        Map<String, List<String>> result = EvalSetValidator.findBrokenCases(List.of(BROKEN_SINGLE), CHUNKS);

        assertEquals(Map.of("broken-1", List.of("존재하지 않는 문구")), result);
    }

    @Test
    @DisplayName("정답 문구 두개인 질문이 깨짐 들어가는 키가 한개 인지 검증")
    void findBrokenCasesTestIsThree() {
        Map<String, List<String>> result = EvalSetValidator.findBrokenCases(List.of(BROKEN_BOTH), CHUNKS);

        assertAll(
                () -> assertEquals(Map.of("broken-2", List.of("없는 문구 하나", "없는 문구 둘")), result),
                () -> assertEquals(1, result.size()),
                () -> assertEquals(2, result.values().stream().mapToInt(List::size).sum())
        );
    }

    @Test
    @DisplayName("정답 문구 둘 중 하나만 깨진 질문")
    void findBrokenCasesTestIsFour() {
        Map<String, List<String>> result = EvalSetValidator.findBrokenCases(List.of(BROKEN_HALF), CHUNKS);

        assertAll(
                () -> assertEquals(Map.of("half-1", List.of("없는 문구")), result),
                () -> assertEquals(1, result.size()),
                () -> assertEquals(1, result.values().stream().mapToInt(List::size).sum())
        );
    }

    @Test
    @DisplayName("여러 질문 중 일부만 깨짐, 키 순서 확인(LinkedMap 으로 인한 순서 보장)")
    void findBrokenCasesTestIsFive() {
        Map<String, List<String>> result = EvalSetValidator.findBrokenCases(List.of(OK_SINGLE, BROKEN_SINGLE, OK_DOUBLE, BROKEN_HALF), CHUNKS);
        List<String> keys = new ArrayList<>(result.keySet());

        assertAll(
                () -> assertEquals(Map.of("broken-1", List.of("존재하지 않는 문구"), "half-1", List.of("없는 문구")), result),
                () -> assertEquals(List.of("broken-1", "half-1"), keys),
                () -> assertEquals(2, result.size()),
                () -> assertEquals(2, result.values().stream().mapToInt(List::size).sum())
        );
    }

    @Test
    @DisplayName("정답 문구가 두 청크에 걸쳐 쪼개짐")
    void findBrokenCasesTestIsSix() {
        Map<String, List<String>> result = EvalSetValidator.findBrokenCases(List.of(SPLIT_ACROSS_CHUNKS), CHUNKS);

        assertAll(
                () -> assertEquals(Map.of("split-1", List.of("서약단원은 오십 명이다")), result),
                () -> assertFalse(result.isEmpty()),
                () -> assertEquals(1, result.size()),
                () -> assertEquals(1, result.values().stream().mapToInt(List::size).sum())
        );
    }

    @Test
    @DisplayName("청크 목록이 비어 있음 정답 문구가 있는 모든 질문이 깨짐")
    void findBrokenCasesTestIsSeven() {
        Map<String, List<String>> result = EvalSetValidator.findBrokenCases(List.of(OK_SINGLE, BROKEN_SINGLE), List.of());
        assertAll(
                () -> assertEquals(Map.of("ok-1", List.of("마정석 한 개는 금화 두 닢에 거래된다"), "broken-1", List.of("존재하지 않는 문구")), result),
                () -> assertEquals(2, result.size()),
                () -> assertEquals(2, result.values().stream().mapToInt(List::size).sum())
        );
    }

    @Test
    @DisplayName("질문 목록이 비어 있음")
    void findBrokenCasesTestIsEight() {
        Map<String, List<String>> result = EvalSetValidator.findBrokenCases(List.of(), CHUNKS);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("정답문구가 존재하지 않을 경우 FALSE, stream 특성상 빈 문자열이면 예외를 발생함")
    void isAnchorsInChunksTestIsOne() {
        boolean anchorInChunk = EvalSetValidator.isAnchorInChunk("1111", CHUNKS);

        assertAll(
                () -> assertFalse(anchorInChunk),
                () -> assertThrows(IllegalArgumentException.class, () -> EvalSetValidator.isAnchorInChunk("", CHUNKS))
        );
    }

    @Test
    @DisplayName("정답문구가 존재할 경우 TRUE")
    void isAnchorsInChunksTestIsTwo() {
        boolean anchorInChunk = EvalSetValidator.isAnchorInChunk("호수 접근은 금지되어 있다.", CHUNKS);

        assertTrue(anchorInChunk);
    }

    @Test
    @DisplayName("청크 크기별 비교 깨진 문구 없음, 모든 질문 반환")
    void commonCasesTestIsOne() {
        List<RagEvalCase> result = EvalSetValidator.commonCases(QUESTIONS, List.of(BROKEN_AT_800));

        assertAll(
                () -> assertFalse(result.isEmpty()),
                () -> assertEquals(List.of(Q1, Q2, Q3, Q4), result),
                () -> assertEquals(4, result.size())
        );
    }

    @Test
    @DisplayName("한 크기에만 깨짐, 순서까지 비교")
    void commonCasesTestIsTwo() {
        List<RagEvalCase> result = EvalSetValidator.commonCases(QUESTIONS, List.of(BROKEN_AT_800, BROKEN_AT_400));

        assertAll(
                () -> assertFalse(result.isEmpty()),
                () -> assertEquals(List.of(Q1, Q3, Q4), result),
                () -> assertEquals(3, result.size())
        );
    }

    @Test
    @DisplayName("같은 질문이 여러 크기에서 깨짐")
    void commonCasesTestIsThree() {
        List<RagEvalCase> result = EvalSetValidator.commonCases(QUESTIONS, List.of(BROKEN_AT_800, BROKEN_AT_400, BROKEN_AT_200));

        assertAll(
                () -> assertFalse(result.isEmpty()),
                () -> assertEquals(List.of(Q1, Q3), result),
                () -> assertEquals(2, result.size())
        );
    }

    @Test
    @DisplayName("깨진 Map 목록이 비어 있음")
    void commonCasesTestIsFour() {
        List<RagEvalCase> result = EvalSetValidator.commonCases(QUESTIONS, List.of());

        assertAll(
                () -> assertFalse(result.isEmpty()),
                () -> assertEquals(List.of(Q1, Q2, Q3, Q4), result),
                () -> assertEquals(4, result.size())
        );
    }

    @Test
    @DisplayName("모든 질문이 깨짐")
    void commonCasesTestIsFive() {
        List<RagEvalCase> result = EvalSetValidator.commonCases(QUESTIONS, List.of(BROKEN_ALL));

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("질문 목록이 비어 있으면 빈 목록")
    void commonCasesTestIsSix() {
        List<RagEvalCase> result = EvalSetValidator.commonCases(List.of(), List.of(BROKEN_AT_400));

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("크기마다 깨진 질문이 서로 다름")
    void commonCasesTestIsSeven() {
        List<RagEvalCase> result = EvalSetValidator.commonCases(QUESTIONS, List.of(BROKEN_AT_400, BROKEN_AT_300));

        assertAll(
                () -> assertFalse(result.isEmpty()),
                () -> assertEquals(List.of(Q1, Q4), result),
                () -> assertEquals(2, result.size())
        );
    }
}
