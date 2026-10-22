package com.worldtaletalk.story.rag;

import java.util.List;

/**
 * eval-set.json 한 건(질문 1개와 정답 근거 문구)을 담는 레코드
 * 유저 입력으로 검색했을 때 정답 근거 문구가 든 청크를 가져오는지 검증하기 위함
 * (정답을 청크 ID가 아니라 문구로 두는 이유는 RetrievalMetrics 참고)
 *
 * @param id              질문 식별자 (예: e-001)
 * @param world           평가 대상 설정집 이름 (현재는 elfheim 하나)
 * @param type            질문 유형 (DIRECT 직접 언급 / PARAPHRASE 다른 표현 / ACTION 행동으로 암시 / BOUNDARY 청크 경계에 걸침)
 * @param query           유저 입력으로 가정한 질문
 * @param expectedAnchors 검색된 청크에 들어 있어야 하는 정답 근거 문구 (설정집 원문과 글자까지 같아야 함)
 */
public record RagEvalCase(
        String id,
        String world,
        String type,
        String query,
        List<String> expectedAnchors
) {}
