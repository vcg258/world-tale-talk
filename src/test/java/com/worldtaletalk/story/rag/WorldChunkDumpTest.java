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

// TokenTextSplitter 기본 설정이 평가용 설정집을 어디서 어떻게 자르는지 확인하기 위한 테스트
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