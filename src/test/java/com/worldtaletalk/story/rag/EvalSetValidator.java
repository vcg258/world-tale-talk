package com.worldtaletalk.story.rag;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.*;

/**
 * 평가 세트의 정답 문구가 청크 안에 온전히 들어 있는지 검증하는 유틸
 * (청크 크기를 바꾸면 정답 문구가 청크 경계에서 쪼개질 수 있어서, 깨진 질문을 가려내는 기준을 한 곳에 모음)
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EvalSetValidator {

    /**
     * 어떤 청크에도 온전히 없는 정답 문구가 있는 질문을 찾아 질문 id별로 모아서 반환함
     * 한 질문에 깨진 문구가 여러 개여도 id는 한 번만 담기고, 입력 순서가 유지됨
     *
     * @param ragEvalCases 평가 질문 목록
     * @param chunks       정답 문구를 찾을 청크 텍스트 목록
     * @return 깨진 질문 id와 그 질문의 깨진 정답 문구 목록 (깨진 질문이 없으면 빈 Map)
     * @throws IllegalArgumentException 정답 문구가 비어 있는 경우 (RetrievalMetrics.contains가 던짐)
     */
    static Map<String, List<String>> findBrokenCases(List<RagEvalCase> ragEvalCases, List<String> chunks) {
        Map<String, List<String>> brokenMap = new LinkedHashMap<>();

        ragEvalCases.forEach(rCase -> rCase.expectedAnchors().forEach(anchor -> {
            boolean exists = isAnchorInChunk(anchor, chunks);
            if (!exists) {
                brokenMap.computeIfAbsent(rCase.id(),  k -> new ArrayList<>()).add(anchor);
            }
        }));

        return brokenMap;
    }

    /**
     * 정답 문구가 청크 하나 안에 온전히 들어 있는지 확인함
     * 두 청크에 걸쳐 쪼개진 문구는 어느 청크에도 온전히 없으므로 false
     *
     * @param anchor 정답 문구
     * @param chunks 청크 텍스트 목록
     * @return 문구를 온전히 담은 청크가 하나라도 있으면 true
     * @throws IllegalArgumentException 정답 문구가 비어 있는 경우 (RetrievalMetrics.contains가 던짐)
     */
    static boolean isAnchorInChunk(String anchor, List<String> chunks) {
        return chunks.stream().anyMatch(chunk -> RetrievalMetrics.contains(chunk, anchor));
    }

    /**
     * 청크 크기별 깨진 질문 결과에서, 어느 크기에서도 깨지지 않은 질문만 골라서 반환함
     * 크기마다 살아남는 질문이 다르면 서로 다른 질문의 점수를 비교하게 되므로, 모든 크기에서 정답 문구가 살아 있는 질문만 남겨서 같은 질문끼리 비교하려고 만듦
     * 한 질문이 여러 크기에서 깨져도 한 번만 제외되고, 남은 질문의 순서는 입력 순서가 유지됨
     *
     * @param ragEvalCases 평가 질문 전체 목록
     * @param brokenCases  크기마다 findBrokenCases가 돌려준 결과 목록 (깨진 질문 id와 문구)
     * @return 모든 크기에서 정답 문구가 살아 있는 질문 목록 (깨진 결과가 없으면 입력 질문 전체)
     */
    static List<RagEvalCase> commonCases(List<RagEvalCase> ragEvalCases, List<Map<String, List<String>>> brokenCases) {
        Set<String> brokenIds = new HashSet<>();

        for (Map<String, List<String>> brokenMap : brokenCases) {
            brokenIds.addAll(brokenMap.keySet());
        }
        return ragEvalCases.stream()
                .filter(rCase -> !brokenIds.contains(rCase.id()))
                .toList();
    }
}
