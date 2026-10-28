package com.worldtaletalk.story.rag;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * RAG 검색 결과를 채점하는 지표 계산 유틸 (순위, recall@k, 역수 순위)
 * <p>
 * 정답은 청크 ID가 아니라 정답 근거 문구(anchor)가 청크에 포함되는지로 판단함
 * (세계관을 다시 인덱싱하거나 청크 크기를 바꾸면 청크 ID와 경계가 달라지기 때문에,
 * 문구 기준이어야 같은 평가 세트로 설정 변경 전후를 비교할 수 있음)
 * <p>
 * 유사도는 벡터 검색 단계에서 이미 계산되어 순서대로 들어오며, 이 클래스는 그 순서를 받아 채점만 함
 * (정답 문구가 한 글자라도 다르거나 두 청크에 걸쳐 잘리면 검색이 맞아도 순위는 0이 됨)
 */
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
        requireValidAnchors(anchors);
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
        requireValidAnchors(anchors);
        List<String> topK = retrieved.subList(0, Math.min(k, retrieved.size()));
        long found = anchors.stream()
                .filter(a -> topK.stream().anyMatch(chunk -> contains(chunk, a)))
                .count();
        return (double) found / anchors.size();
    }

    /**
     * 순위 -> 역수. 못 찾으면 0 (순위는 낮을수록 좋고 점수는 높을 수 록 좋으니 역수로 계산)
     *
     * @param rank firstRelevantRank 결과 (1부터, 없으면 0)
     * @return 1/rank, 못 찾으면 0.0
     */
    static double reciprocalRank(int rank) {
        return rank == 0 ? 0.0 : 1.0 / rank;
    }

    /**
     * 정답 순위가 1 ~ k위 안일 때만 역수 점수를 구함 (k위 밖이거나 못 찾은 경우(0)는 0.0)
     * (운영은 topK 5개만 LLM에 전달하므로 6위부터는 찾지 못한 것과 같게 처리하기 위함)
     * topK가 변경되면 수치가 변경될 수 있음
     *
     * @param rank 정답 문구가 처음 나온 순위 (1부터, 못 찾으면 0)
     * @param k    인정할 순위 범위 (운영 topK)
     * @return 1/rank, 범위 밖이면 0.0
     */
    static double reciprocalRankAtK(int rank, int k) {
        return rank >= 1 && rank <= k ? reciprocalRank(rank) : 0.0;
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

    /**
     * 정답 근거 목록이 비어있거나 공백일 경우 예외발생 하기 위한 헬퍼 메서드
     * (빈 문구는 어떤 청크에도 포함되는 것으로 판정돼 순위가 1위로 부풀려지고, 오류도 없이 지나가기 때문에
     * 평가 세트의 오타나 누락을 조기에 발견하기 위해 입구에서 먼저 막음)
     *
     * @param anchors 정답 근거 목록
     */
    private static void requireValidAnchors(List<String> anchors) {
        if (anchors.isEmpty()) {
            throw new IllegalArgumentException("anchors list is empty");
        }
        if (anchors.stream().anyMatch(String::isBlank)) {
            throw new IllegalArgumentException("anchors must not be blank");
        }
    }
}
