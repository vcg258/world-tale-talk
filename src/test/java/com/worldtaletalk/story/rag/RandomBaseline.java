package com.worldtaletalk.story.rag;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.*;


/**
 * 청크를 무작위로 섞어서 채점했을 때의 평균 점수를 구하는 유틸
 * (실제 검색 점수가 우연보다 나은지 비교할 기준선이 필요해서 만듦)
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RandomBaseline {
    /**
     * 질문마다 청크 순서를 무작위로 섞어 채점하는 과정을 trials번 반복하고, 유형별과 전체(ALL)의 평균 점수를 구함
     * 한 번만 섞으면 운 좋게 맞은 결과가 나올 수 있어서 여러 번 반복해 평균을 냄
     * 질문마다 따로 섞는 이유는 한 번 섞은 순서를 모든 질문에 쓰면 질문 간 결과가 서로 묶이기 때문이고,
     * 채점은 실제 검색 결과와 같은 EvalReport.scoreCase를 써서 점수 계산 방식을 똑같이 맞춤
     *
     * @param ragEvalCases 평가 질문 목록
     * @param chunkTexts   섞을 대상인 청크 텍스트 (원본은 바꾸지 않고 복사본을 섞음)
     * @param trials       반복 횟수 (많을수록 평균이 안정됨)
     * @param seed         난수 시드 (같은 값이면 같은 결과가 나와서 재현 가능)
     * @return 유형별 평균 줄들과 마지막 ALL 줄 (EvalReport.averageByType과 같은 순서)
     * @throws IllegalArgumentException trials가 1보다 작은 경우 (평균을 나눌 수 없기 때문)
     */
    static List<TypeSummary> simulate(List<RagEvalCase> ragEvalCases,
                                      List<String> chunkTexts,
                                      int trials,
                                      long seed
    ) {
        if (trials <= 0) {
            throw new IllegalArgumentException("시행값은 1이상이어야함 예외 trials : " + trials);
        }
        if (chunkTexts.isEmpty()) {
            throw new IllegalArgumentException("청크가 비었음");
        }

        List<TypeSummary> averaged = new ArrayList<>();
        // 줄 이름별 지표 합계 [recall@1, recall@3, recall@5, MRR@5] (평균은 마지막에 한 번만 나눔)
        Map<String, double[]> sums = new LinkedHashMap<>();
        Map<String, Integer> counts = new HashMap<>();

        // 루프 안에서 만들면 매번 같은 순서로 섞이므로 밖에서 한 번만 만듦
        Random random = new Random(seed);
        for (int i = 0; i < trials; i++) {
            // 질문마다 따로 섞고 채점
            List<CaseResult> results = new ArrayList<>();
            for (RagEvalCase r : ragEvalCases) {
                List<String> shuffled = new ArrayList<>(chunkTexts);
                Collections.shuffle(shuffled, random);
                results.add(EvalReport.scoreCase(r, shuffled));
            }

            // 이번 시도의 유형별 평균을 줄 이름별 합계에 더함
            List<TypeSummary> summaries = EvalReport.averageByType(results);
            for (TypeSummary summary : summaries) {
                double[] acc = sums.computeIfAbsent(summary.name(), k -> new double[4]);
                acc[0] += summary.recallAvg1();
                acc[1] += summary.recallAvg3();
                acc[2] += summary.recallAvg5();
                acc[3] += summary.mrrAvg5();
                counts.putIfAbsent(summary.name(), summary.queryCount());
            }
        }

        // 합계를 시도 횟수로 나눠 평균 줄로 변환
        for (Map.Entry<String, double[]> entry : sums.entrySet()) {
            String name = entry.getKey();
            double[] acc = entry.getValue();

            averaged.add(new TypeSummary(
                    name,
                    counts.get(name),
                    acc[0] / trials,
                    acc[1] / trials,
                    acc[2] / trials,
                    acc[3] / trials
            ));
        }
        return averaged;
    }
}
