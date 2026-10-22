package com.worldtaletalk.story.rag;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * TokenTextSplitter 기본 설정이 평가용 설정집(elfheim.txt)을 어디서 어떻게 자르는지 확인하기 위한 테스트 (assert 없이 로그 확인용)
 * <p>
 * 확인 결과 (Spring AI 1.1.6 기본 생성자): 청크 크기 800토큰, 오버랩 없음, 마침표 기준으로 끊음
 * 한글은 대략 1토큰 = 1글자라 청크가 700~800자로 나오고(6개), 문서 구조를 모르기 때문에
 * 제목과 본문이 갈라지거나 한 인물 설명이 두 청크로 나뉘는 경우가 있음
 * -> 평가 세트의 정답 문구를 짧게 잡고 경계에서 먼 곳으로 잡은 이유
 */
@Slf4j
class WorldChunkDumpTest {

    @Test
    void chunkDump() throws IOException {
        String text;
        try (InputStream in = new ClassPathResource("rag-eval/worlds/elfheim.txt").getInputStream()) {
            text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        List<Document> chunks = new TokenTextSplitter().apply(List.of(new Document(text)));
        log.info("원문 : {}, 청크 수 : {}", text, chunks.size());

        for (int i = 0; i < chunks.size(); i++) {
            String chunk = chunks.get(i).getText();
            log.info("===== chunk {} ({}자) =====\n{}", i, chunk.length(), chunk);
        }
    }
}