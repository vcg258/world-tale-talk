package com.worldtaletalk.story.rag;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 평가용 설정집(elfheim.txt)을 eval 전용 worldId로 인덱싱한 뒤, 운영과 같은 WorldSettingRetriever로 검색해서
 * RAG 검색 품질을 확인하기 위한 평가 러너
 * <p>
 * 실제 세계관 청크를 건드리지 않기 위해 테스트마다 평가용 청크를 먼저 삭제하고 끝나면 다시 삭제함
 * (이전 실행이 중간에 중단돼도 평가용 청크가 쌓이지 않게 하기 위함)
 * OpenAI 임베딩 API를 호출하므로 일반 테스트와 구분하려고 rag-eval 태그를 붙임
 */
@Slf4j
@Tag("rag-eval")
@SpringBootTest
public class WorldSettingRetrievalEvalTest {
    @Autowired
    private VectorStore vectorStore;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private WorldSettingRetriever worldSettingRetriever; // 운영과 같은 검색 경로로 평가하기 위해 주입
    @Autowired
    private ObjectMapper objectMapper;
    private static final Long EVAL_WORLD_ID = -1L; // 실제 worldId와 겹치지 않게 음수로 지정(실제값 양수)
    private List<String> chunkTexts; // 청크 번호 리스트 (인덱스)
    private int othersBefore; // 테스트 전 기존(실제 세계관) 청크 수 (테스트 후 변화가 없는지 비교용)

    /**
     * 테스트를 시작할때 현재 프로젝트 루트 경로에 존재하는 elfheim.txt를 청킹해서 VectorStore에 넣음
     *
     * @throws IOException 파일 청킹 예외
     */
    @BeforeEach
    void indexEvalWorld() throws IOException {
        deleteEvalIndex(); // 이전 실행이 중간에 중단돼서 남은 평가용 청크가 있을 수 있어 먼저 삭제
        othersBefore = countOthers(); // 평가용 청크를 넣기 전에 기존 청크 수를 기록

        String text;
        try (InputStream in = new ClassPathResource("rag-eval/worlds/elfheim.txt").getInputStream()) {
            text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        // 운영 indexWorld와 같은 기본 분할기(TokenTextSplitter)로 청킹
        List<Document> chunks = new TokenTextSplitter().apply(List.of(new Document(text)));
        chunkTexts = chunks.stream().map(Document::getText).toList();

        // 운영 indexWorld와 같은 메타데이터 구조(sourceType, sourceId)로 저장
        List<Document> docs = chunks.stream()
                .map(c -> new Document(c.getText(), Map.of(
                        "sourceType", "WORLD_SETTING",
                        "sourceId", EVAL_WORLD_ID.toString())))
                .toList();
        vectorStore.add(docs);
        log.info("평가용 청크 인덱싱 완료: {}", docs.size());
    }

    /**
     * 기존 세계관 값들을 건들지 않기 위해 테스트 청크 삭제 메서드
     */
    @AfterEach
    void cleanUp() {
        deleteEvalIndex();
        log.info("평가용 청크 삭제 완료");
    }

    @Test
    @DisplayName("인덱싱하면 6개가 들어가고 삭제하면 0개가 되며 기존 데이터는 기대로")
    void indexAndCleanUp() {
        assertEquals(6, countBySourceId(EVAL_WORLD_ID.toString())); // 평가용 청크가 실제로 들어갔는지
        assertEquals(othersBefore, countOthers()); // 넣는 과정에서 기존 청크가 변하지 않았는지

        deleteEvalIndex();
        assertEquals(0, countBySourceId(EVAL_WORLD_ID.toString())); // 평가용 청크만 삭제됐는지
        assertEquals(othersBefore, countOthers()); // 삭제가 기존 청크를 건드리지 않았는지
    }

    /**
     * 질문 1개로 검색해서 반환 순서, 유사도, 정답 문구의 순위를 확인
     * (점수 평가 전에 검색 파이프라인이 정상 동작하는지 눈으로 확인하기 위함)
     */
    @Test
    @DisplayName("질문 1개로 검색하면 청크가 유사도 순으로 반환된다")
    void searchOrderForOneQuery() {
        String query = "마정석 거래는 하나에 얼마인가?";
        List<String> anchors = List.of("마정석 한 개는 시중에서 금화 두 닢"); // 설정집 원문과 글자까지 같아야 매칭됨

        // 운영 topK(5)가 아니라 청크 수만큼 요청해서 전체 순위를 확인
        List<Document> docs = worldSettingRetriever.retrieve(EVAL_WORLD_ID, query, chunkTexts.size());

        log.info("질문: {} / 반환 {}개", query, docs.size());

        for (int i = 0; i < docs.size(); i++) {
            Document d = docs.get(i);
            // 검색된 텍스트가 원래 몇 번째 청크인지 번호로 변환해서 출력
            log.info("{}위 -> chunk {} (유사도 {})", i + 1, chunkNumberOf(d.getText()), d.getScore());
        }

        List<String> texts = docs.stream().map(Document::getText).toList();
        int rank = RetrievalMetrics.firstRelevantRank(texts, anchors); // 정답 문구가 처음 나온 순위 (없으면 0)
        log.info("정답 순위 {}", rank);

        assertEquals(chunkTexts.size(), docs.size()); // 청크가 모두 반환되는지
    }

    @Test
    @DisplayName("eval-set.json을 읽으면 31건이 불러와짐")
    void loadEvalCasesReadsAllCases() throws IOException {
        List<RagEvalCase> ragEvalCases = loadEvalCases();

        assertAll(
                () -> assertEquals(31, ragEvalCases.size()), // 현재 평가세트는 31개
                // 첫번째 Id가 e-001로 순서가 정확히 나오는지 확인
                () -> assertEquals("e-001", ragEvalCases.getFirst().id()),
                // expectedAnchors빈값이 존재하는지 아닌지 확인
                () -> assertTrue(ragEvalCases.stream().noneMatch(r -> r.expectedAnchors().isEmpty()),
                        "expectedAnchors가 비어있는 질문 존재함")
        );
    }

    @Test
    @DisplayName("청크에 평가세트JSON에 있는 정답 문구가 모두 들어있으므로 예외가 발생하지 않음")
    void validateAnchorsPassesForEvalSet() throws IOException {
        List<RagEvalCase> ragEvalCases = loadEvalCases();

        assertDoesNotThrow(() -> validateAnchorsExistInChunks(ragEvalCases));
    }

    @Test
    @DisplayName("청크에 평가세트JSON에 있는 정답 문구가 들어있지 않음 예외 발생")
    void validateAnchorsFailsWhenAnchorIsMissing() {
        RagEvalCase ragEvalCase = new RagEvalCase(
                "f-001",
                "321",
                "546",
                "존재하지 않음",
                List.of("존재하지 않는 문구")
        );

        IllegalStateException e = assertThrows(IllegalStateException.class, () -> validateAnchorsExistInChunks(List.of(ragEvalCase)),
                "청크에 없는 정답 문구가 있으면 예외가 발생해야 함"
        );
        assertTrue(e.getMessage().contains(ragEvalCase.id()));
    }

    @Test
    @DisplayName("질문 1건을 채점하면 id와 유형이 그대로 담기고 순위 범위와 recall 순서가 맞게 나옴")
    void evaluateCaseReturnsConsistentMetrics() throws IOException {
        RagEvalCase ragEvalCase = loadEvalCases().getFirst();

        CaseResult caseResult = evaluateCase(ragEvalCase);

        assertAll(
                () -> assertEquals("e-001", caseResult.id()),
                () -> assertEquals("DIRECT", caseResult.type()),
                () -> assertTrue(caseResult.rank() >= 1),
                () -> assertTrue(caseResult.rank() <= chunkTexts.size()),
                () -> assertTrue(caseResult.recallAt1() >= 0.0 && caseResult.recallAt1() <= 1.0),
                () -> assertTrue(caseResult.recallAt3() >= 0.0 && caseResult.recallAt3() <= 1.0),
                () -> assertTrue(caseResult.recallAt5() >= 0.0 && caseResult.recallAt5() <= 1.0),
                () -> assertTrue(caseResult.recallAt1() <= caseResult.recallAt3()),
                () -> assertTrue(caseResult.recallAt3() <= caseResult.recallAt5()),
                () -> assertTrue(caseResult.mrrAt5() >= 0.0 && caseResult.mrrAt5() <= 1.0)
        );
    }

    @Test
    @DisplayName("평가 세트 전체를 채점해서 질문별 결과를 로그로 출력하고 결과 개수와 id 순서가 평가 세트와 같음")
    void runBaseline() throws IOException {
        // 평가 세트 불러오기
        List<RagEvalCase> ragEvalCases = loadEvalCases();
        List<String> answerIdx = ragEvalCases.stream().map(RagEvalCase::id).toList();
        // 정답 문구 오타는 점수 계산 전에 먼저 걸러냄
        validateAnchorsExistInChunks(ragEvalCases);
        // 질문별 채점
        List<CaseResult> results = ragEvalCases.stream().map(r -> evaluateCase(r)).toList();
        List<String> resultIdx = results.stream().map(CaseResult::id).toList();
        // 질문별 rank, recall, mrr 로그
        results.forEach(c ->
            log.info("id={} type={} rank={} r@1={} r@3={} r@5={} mrr@5={}",
                    c.id(),
                    c.type(),
                    c.rank(),
                    c.recallAt1(),
                    c.recallAt3(),
                    c.recallAt5(),
                    c.mrrAt5()
            ));

        // 타입별 recall, mrr 평균
        List<TypeSummary> summaries = EvalReport.averageByType(results);
        summaries.forEach(s ->
                log.info("name={}, queryCount={}, r@1={}, r@3={}, r@5={}, mrr@5={}",
                        s.name(),
                        s.queryCount(),
                        s.recallAvg1(),
                        s.recallAvg3(),
                        s.recallAvg5(),
                        s.mrrAvg5()
                ));
        // 결과 개수와 id 순서가 평가 세트와 같은지 검증
        assertEquals(answerIdx, resultIdx);
    }


    /* Helper Method */

    /**
     * 평가용 청크만 삭제 (sourceType, sourceId 필터로 실제 세계관 청크는 건드리지 않음)
     */
    private void deleteEvalIndex() {
        FilterExpressionBuilder b = new FilterExpressionBuilder();
        vectorStore.delete(b.and(
                        b.eq("sourceType", "WORLD_SETTING"),
                        b.eq("sourceId", EVAL_WORLD_ID.toString()))
                .build()
        );
    }

    /**
     * vector_store에서 해당 sourceId를 가진 청크 수 조회
     *
     * @param sourceId 세계관 PK (메타데이터에 문자열로 저장됨)
     * @return 청크 수
     */
    private int countBySourceId(String sourceId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM vector_store WHERE metadata->>'sourceId' = ?",
                Integer.class, sourceId
        );
        return count == null ? 0 : count;
    }

    /**
     * 평가용 worldId를 제외한 기존 청크 수 조회
     * (sourceId가 없는 행도 포함하기 위해 <>가 아닌 IS DISTINCT FROM 사용)
     *
     * @return 기존 청크 수
     */
    private int countOthers() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM vector_store WHERE metadata->>'sourceId' IS DISTINCT FROM ?",
                Integer.class, EVAL_WORLD_ID.toString()
        );
        return count == null ? 0 : count;
    }

    /**
     * 검색된 텍스트가 원본 청크 중 몇 번째인지 조회 (공백 차이는 정규화로 흡수)
     *
     * @param text 검색된 청크 텍스트
     * @return 청크 번호 (0부터), 일치하는 청크가 없으면 -1
     */
    private int chunkNumberOf(String text) {
        for (int i = 0; i < chunkTexts.size(); i++) {
            if (RetrievalMetrics.normalize(chunkTexts.get(i)).equals(RetrievalMetrics.normalize(text))) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 평가 세트 JSON 값을 모두 자바코드로 역직렬화
     *
     * @return 평가세트 질문 목록
     * @throws IOException 파일 입출력 예외
     */
    private List<RagEvalCase> loadEvalCases() throws IOException {
        try (InputStream in = new ClassPathResource("rag-eval/eval-set.json").getInputStream()) {
            String text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            return objectMapper.readValue(text, new TypeReference<List<RagEvalCase>>() {
            });
        }
    }


    /**
     * 평가세트 JSON의 정답 문구가 청크에도 있는지 없는지 구분하기 위한 헬퍼메서드
     *
     * @param ragEvalCases 평가세트
     */
    private void validateAnchorsExistInChunks(List<RagEvalCase> ragEvalCases) {
        List<String> problems = new ArrayList<>();
        ragEvalCases.forEach(rCase -> rCase.expectedAnchors().forEach(anchor -> {
            boolean exists = chunkTexts.stream().anyMatch(chunk -> RetrievalMetrics.contains(chunk, anchor));
            if (!exists) {
                problems.add(rCase.id() + " : " + anchor);
            }
        }));

        if (!problems.isEmpty()) {
            throw new IllegalStateException("어떤 청크에도 없는 정답 문구가 있음:\n" + String.join("\n", problems));
        }
    }

    /**
     * 질문 1건을 실제로 검색해서 정답 문구의 순위와 recall(1, 3, 5)을 구하고 결과로 묶음
     * (운영 topK(5)가 아니라 청크 수만큼 요청해서 전체 순위를 받음. 6위와 아예 못 찾은 경우(0)를 구분하기 위함)
     *
     * @param ragEvalCase 평가 세트 질문 1건
     * @return 질문 id, 유형, 정답 문구가 처음 나온 순위, recall@1/3/5
     */
    private CaseResult evaluateCase(RagEvalCase ragEvalCase) {
        List<String> anchors = ragEvalCase.expectedAnchors();
        List<Document> docs = worldSettingRetriever.retrieve(EVAL_WORLD_ID, ragEvalCase.query(), chunkTexts.size());
        List<String> texts = docs.stream().map(Document::getText).toList();
        int rank = RetrievalMetrics.firstRelevantRank(texts, anchors);
        double mrrAt5 = RetrievalMetrics.reciprocalRankAtK(rank, WorldSettingRetriever.DEFAULT_TOP_K);
        double recall1 = RetrievalMetrics.recallAtK(texts, anchors, 1);
        double recall3 = RetrievalMetrics.recallAtK(texts, anchors, 3);
        double recall5 = RetrievalMetrics.recallAtK(texts, anchors, 5);

        return new CaseResult(
                ragEvalCase.id(),
                ragEvalCase.type(),
                rank,
                recall1,
                recall3,
                recall5,
                mrrAt5
        );
    }
}
