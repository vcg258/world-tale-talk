package com.worldtaletalk.story.rag;

/**
 * 평가 세트 질문 1건의 채점 결과 (유형별 평균과 결과 표의 재료로 쓰임)
 *
 * @param id        질문 식별자 (어느 질문의 점수인지 알기 위함)
 * @param type      질문 유형 (유형별 평균을 내기 위함)
 * @param rank      정답 문구가 처음 나온 순위 (1부터, 어느 청크에도 없으면 0)
 * @param recallAt1 상위 1개 안에서 찾은 정답 문구 비율
 * @param recallAt3 상위 3개 안에서 찾은 정답 문구 비율
 * @param recallAt5 상위 5개 안에서 찾은 정답 문구 비율
 * @param mrrAt5    운영 topK(5) 기준 역수 순위 점수 (1~5위는 1/순위, 그 밖은 0.0)
 */
public record CaseResult(
        String id,
        String type,
        int rank,
        double recallAt1,
        double recallAt3,
        double recallAt5,
        double mrrAt5
) {}
