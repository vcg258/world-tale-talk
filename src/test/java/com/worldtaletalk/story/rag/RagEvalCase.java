package com.worldtaletalk.story.rag;

import java.util.List;

// eval-set.json(평가 셋)을 담아줄 레코더(사용자가 프롬프트를 요청값으로 보낼때 알맞은 청크를 검색해 가져오는지 검증하기 위함)
public record RagEvalCase(
        String id,
        String world,
        String type,
        String query,
        List<String> expectedAnchors
) {}
