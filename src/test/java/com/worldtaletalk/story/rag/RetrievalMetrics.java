package com.worldtaletalk.story.rag;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RetrievalMetrics {
    /**
     * 정답 문구가 처음 나온 청크의 순위 (1부터), 없으면 0
     *
     * @param retrieved 검색된 청크 텍스트 목록 (유사도 높은 순으로)
     * @param anchors   정답 근거 문구 목록 (하나라도 청크 포함이면 정답으로 봄)
     * @return 청크의 순위 (1부터), 없으면 0
     */
    static int firstRelevantRank(List<String> retrieved, List<String> anchors) {
        for (int i = 0; i < retrieved.size(); i++) {
            String chunk = retrieved.get(i);
            if (anchors.stream().anyMatch(a -> contains(chunk, a))) {
                return i + 1;
            }
        }
        return 0;
    }

    /**
     * 상위 k개 안에서 찾은 정답 문구의 비율 (0.0 ~ 1.0)
     *
     * @param retrieved 검색된 청크 텍스트 목록 (유사도 높은 순)
     * @param anchors   정답 근거 문구 목록
     * @param k         앞에서 부터 어디까지 볼지
     * @return 정답 문구 중 상위 k개 안에서 찾은 비율
     */
    static double recallAtK(List<String> retrieved, List<String> anchors, int k){
        List<String> topK = retrieved.subList(0, Math.min(k, retrieved.size()));
        if (anchors.isEmpty()) {
            throw new IllegalArgumentException("anchors list is empty");
        }
        long found = anchors.stream()
                .filter(a -> topK.stream().anyMatch(chunk -> contains(chunk, a)))
                .count();
        return (double) found / anchors.size();
    }

    /**
     * 순위 -> 역수. 못 찾으면 0
     *
     * @param rank firstRelevantRank 결과 (1부터, 없으면 0)
     * @return 1/rank, 못 찾으면 0.0
     */
    static double reciprocalRank(int rank) {
        return rank == 0 ? 0.0 : 1.0 / rank;
    }

    /**
     * 줄바꿈/연속 공백 차이로 매칭이 실패하지 않도록 정규화
     *
     * @param text 정규화할 문자열
     * @return 연속 공백이 한 칸으로 줄고 앞뒤 공백이 제거된 문자열
     */
    static String normalize(String text) {
        return text.replaceAll("\\s+", " ").trim();
    }

    /**
     * 청크에 정답 문구가 들어 있는가
     *
     * @param chunk  청크 값
     * @param anchor 정답 근거 문구
     * @return 청크에 문구가 포함되면 true
     */
    static boolean contains(String chunk, String anchor) {
        if (anchor.isBlank()) {
            throw new IllegalArgumentException("anchor must not be empty");
        }
        return normalize(chunk).contains(normalize(anchor));
    }
}
