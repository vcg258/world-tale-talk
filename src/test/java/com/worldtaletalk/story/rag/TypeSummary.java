package com.worldtaletalk.story.rag;

/**
 * 질문 유형별(DIRECT, PARAPHRASE 등)과 전체(ALL)의 평균 점수 (결과 표의 한 줄)
 * 질문 1건의 점수(CaseResult)를 묶어서 어느 유형이 약한지 한눈에 보기 위함
 * (ALL은 유형 평균의 평균이 아니라 전체 질문을 한꺼번에 평균 낸 값)
 *
 * @param name        유형 이름 (전체는 ALL)
 * @param queryCount  평균에 포함된 질문 수 (질문 수가 적은 유형은 점수가 크게 흔들리므로 함께 확인하기 위함)
 * @param recallAvg1  recall@1 평균
 * @param recallAvg3  recall@3 평균
 * @param recallAvg5  recall@5 평균
 * @param mrrAvg5     운영 topK(5) 기준 MRR@5 평균
 */
public record TypeSummary(
        String name,
        int queryCount,
        double recallAvg1,
        double recallAvg3,
        double recallAvg5,
        double mrrAvg5
) {}
