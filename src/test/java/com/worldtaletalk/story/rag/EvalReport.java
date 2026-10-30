package com.worldtaletalk.story.rag;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 질문별 채점 결과(CaseResult)를 유형별과 전체로 묶어서 평균 점수표로 만드는 유틸
 * (질문 1건의 점수만 봐서는 어느 유형이 약한지 알 수 없어서 유형별로 묶어 비교하기 위함)
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EvalReport {
    /**
     * 질문별 결과를 유형별로 묶어 평균을 내고 마지막에 전체(ALL) 평균을 붙임
     * 유형 줄의 순서는 입력에 처음 나온 유형 순서이고, ALL은 유형 평균의 평균이 아니라
     * 전체 질문을 한꺼번에 평균 낸 값임 (질문이 3건뿐인 유형이 전체 점수의 25%를 좌우하지 않게 하기 위함)
     *
     * @param caseResults 질문별 채점 결과
     * @return 유형별 평균 줄들과 마지막 ALL 줄
     * @throws IllegalArgumentException 채점 결과가 비어 있는 경우
     */
    static List<TypeSummary> averageByType(List<CaseResult> caseResults) {
        Map<String, List<CaseResult>> byType = new LinkedHashMap<>();
        List<TypeSummary> summaries = new ArrayList<>();
        for (CaseResult caseResult : caseResults) {
            byType.computeIfAbsent(caseResult.type(), t -> new ArrayList<>()).add(caseResult);
        }
        for (Map.Entry<String, List<CaseResult>> entry : byType.entrySet()) {
            String type = entry.getKey();
            List<CaseResult> results = entry.getValue();
            TypeSummary summary = summarize(type, results);
            summaries.add(summary);
        }
        summaries.add(summarize("ALL", caseResults));
        return summaries;
    }

    /**
     * 결과 목록 하나의 recall@1/3/5와 MRR@5 평균을 구해 한 줄(TypeSummary)로 만듦
     * (유형별 줄과 전체 줄을 같은 방식으로 계산하려고 분리함)
     *
     * @param name        줄 이름 (유형 이름 또는 ALL)
     * @param caseResults 평균을 낼 채점 결과
     * @return 평균 점수 한 줄
     * @throws IllegalArgumentException 결과가 비어 있는 경우 (평균을 0.0으로 두면 오류가 가려지기 때문)
     */
    private static TypeSummary summarize(String name, List<CaseResult> caseResults) {
        if (caseResults.isEmpty()) {
            throw new IllegalArgumentException("평균낼 결과가 없음 name : " + name);
        }
        double recall1 = caseResults.stream().mapToDouble(CaseResult::recallAt1).average().orElse(0.0);
        double recall3 = caseResults.stream().mapToDouble(CaseResult::recallAt3).average().orElse(0.0);
        double recall5 = caseResults.stream().mapToDouble(CaseResult::recallAt5).average().orElse(0.0);
        double mrr5 = caseResults.stream().mapToDouble(CaseResult::mrrAt5).average().orElse(0.0);

        return new TypeSummary(
                name,
                caseResults.size(),
                recall1,
                recall3,
                recall5,
                mrr5
        );
    }
}