package com.worldtaletalk.story.rag;

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
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@Tag("rag-eval")
@SpringBootTest
public class WorldSettingRetrievalEvalTest {
    @Autowired private VectorStore vectorStore;
    @Autowired private JdbcTemplate jdbcTemplate;
    private static final Long EVAL_WORLD_ID = -1L; // 실제 worldId와 겹치지 않게 음수로 지정(실제값 양수)
    private List<String> chunkTexts; // 청크 번호 리스트 (인덱스)
    private int othersBefore;

    /**
     * 테스트를 시작할때 현재 프로젝트 루트 경로에 존재하는 elfheim.txt를 청킹해서 VectorStore에 넣음
     *
     * @throws IOException 파일 청킹 예외
     */
    @BeforeEach
    void indexEvalWorld() throws IOException {
        deleteEvalIndex();
        othersBefore = countOthers();

        String text;
        try (InputStream in = new ClassPathResource("rag-eval/worlds/elfheim.txt").getInputStream()) {
            text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        List<Document> chunks = new TokenTextSplitter().apply(List.of(new Document(text)));
        chunkTexts = chunks.stream().map(Document::getText).toList();

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
        assertEquals(6, countBySourceId(EVAL_WORLD_ID.toString()));
        assertEquals(othersBefore, countOthers());

        deleteEvalIndex();
        assertEquals(0, countBySourceId(EVAL_WORLD_ID.toString()));
        assertEquals(othersBefore, countOthers());
    }

    /* Helper Method */

    private void deleteEvalIndex() {
        FilterExpressionBuilder b = new FilterExpressionBuilder();
        vectorStore.delete(b.and(
                b.eq("sourceType", "WORLD_SETTING"),
                b.eq("sourceId", EVAL_WORLD_ID.toString()))
                .build()
        );
    }

    private int countBySourceId(String sourceId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM vector_store WHERE metadata->>'sourceId' = ?",
                Integer.class, sourceId
        );
        return count == null ? 0 : count;
    }

    private int countOthers() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM vector_store WHERE metadata->>'sourceId' IS DISTINCT FROM ?",
                Integer.class, EVAL_WORLD_ID.toString()
        );
        return count == null ? 0 : count;
    }
}
