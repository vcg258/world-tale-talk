package com.worldtaletalk.story.rag;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 세계관 설정 청크 검색기 (해당 세계관의 WORLD_SETTING 청크만 대상)
 * <p>
 * StoryAiServiceImpl 안에 있던 검색 로직을 분리함. RAG 검색 품질 평가 코드가 운영과 같은 검색 경로를 호출하게 하기 위함
 * (동작은 분리 전과 같음: 메타데이터 필터, 기본 topK 5, 유사도 임계값 미지정)
 */
@Component
@RequiredArgsConstructor
public class WorldSettingRetriever {
    public static final int DEFAULT_TOP_K = 5;
    private final VectorStore vectorStore;

    /**
     * 세계관 유사도 검색 (해당 세계관 청크만 대상)
     *
     * @param worldId 세계관 PK
     * @param query 유저 입력
     * @param topK 반환 청크 수
     * @return 유사도 순 청크
     */
    public List<Document> retrieve(Long worldId, String query, int topK) {
        FilterExpressionBuilder b = new FilterExpressionBuilder();
        Filter.Expression worldFilter = b.and(
                b.eq("sourceType", "WORLD_SETTING"),
                b.eq("sourceId", worldId.toString())
        ).build();

        return vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .topK(topK)
                        .filterExpression(worldFilter)
                        .build()
        );
    }
}
