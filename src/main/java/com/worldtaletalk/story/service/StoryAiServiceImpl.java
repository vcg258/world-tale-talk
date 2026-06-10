package com.worldtaletalk.story.service;

import com.worldtaletalk.persona.domain.PersonasBaseStatsEntity;
import com.worldtaletalk.persona.domain.PersonasEntity;
import com.worldtaletalk.persona.repository.PersonasBaseStatsRepository;
import com.worldtaletalk.persona.repository.PersonasRepository;
import com.worldtaletalk.session.domain.StorySessionsEntity;
import com.worldtaletalk.session.repository.StorySessionsRepository;
import com.worldtaletalk.story.dto.LlmTurnResult;
import com.worldtaletalk.world.domain.WorldsEntity;
import com.worldtaletalk.world.repository.WorldsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class StoryAiServiceImpl implements StoryAiService {
    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final WorldsRepository worldsRepository;
    private final PersonasRepository personasRepository;
    private final PersonasBaseStatsRepository personasBaseStatsRepository;
    private final StorySessionsRepository storySessionsRepository;

    /**
     * 세계관 삭제
     * @param worldId 세계관 PK
     */
    @Override
    public void deleteWorldIndex(Long worldId) {
        // 세계관 LLM주입 프롬프트만 걸러서 삭제
        FilterExpressionBuilder b = new FilterExpressionBuilder();
        Filter.Expression filter = b.and(
                b.eq("sourceType", "WORLD_SETTING"),
                b.eq("sourceId", worldId.toString())
        ).build();

        SearchRequest searchRequest = SearchRequest.builder()
                .query("world setting")
                .topK(10000) // 최대한 관련있는 청크를 가져오기 위해 높게 설정
                .similarityThreshold(0.0) // 메타데이터 필터를 이미 걸어놨기 때문에 유사도로 검색할 필요 없음
                .filterExpression(filter) // 이녀석이 메타데이터 필터
                .build();

        List<Document> existing = vectorStore.similaritySearch(searchRequest);

        if (!existing.isEmpty()) {
            List<String> ids = existing.stream()
                    .map(Document::getId)
                    .collect(Collectors.toList());
            vectorStore.delete(ids);
            log.info("세계관 인덱스 삭제 완료 worldId: {} 삭제된 청크 수: {}", worldId, ids.size());
        }
    }

    /**
     * 세계관 등록/수정
     * @param worldId 세계관 PK
     */
    @Override
    public void indexWorld(Long worldId) {
        WorldsEntity world = worldsRepository.findById(worldId).orElseThrow(() ->
                new NoSuchElementException("해당 세계관은 존재 하지 않습니다."));
        // 수정시 전에 있던 데이터는 제거
        deleteWorldIndex(worldId);

        // 텍스트 청크 분할
        TokenTextSplitter textSplitter = new TokenTextSplitter();
        List<Document> rawDoc = List.of(new Document(world.getSettingText()));
        List<Document> chunks = textSplitter.apply(rawDoc);

        List<Document> documents = chunks.stream()
                .map(chunk -> new Document(
                        chunk.getText(),
                        Map.of(
                                "sourceType", "WORLD_SETTING",
                                "sourceId", worldId.toString()
                        )
                )).collect(Collectors.toList());

        vectorStore.add(documents);
        log.info("세계관 인덱스 등록 완료 worldId: {} 청크 수: {}", worldId, documents.size());
    }

    /**
     * 페르소나 삭제
     * @param personaId 페르소나 PK
     */
    @Override
    public void deletePersonaIndex(Long personaId) {
        FilterExpressionBuilder b = new FilterExpressionBuilder();
        Filter.Expression filter = b.and(
                b.eq("sourceType", "PERSONA_BACKGROUND"),
                b.eq("sourceId", personaId.toString())
        ).build();

        SearchRequest searchRequest = SearchRequest.builder()
                .query("persona background")
                .topK(10000)
                .similarityThreshold(0.0)
                .filterExpression(filter)
                .build();

        List<Document> existing = vectorStore.similaritySearch(searchRequest);

        if (!existing.isEmpty()) {
            List<String> ids = existing.stream()
                    .map(Document::getId)
                    .collect(Collectors.toList());
            vectorStore.delete(ids);
            log.info("페르소나 인덱스 삭제 완료 personaId: {} 삭제된 청크 수: {}", personaId, ids.size());
        }
    }

    /**
     * 페르소나 등록/수정
     * @param personaId 페르소나 PK
     */
    @Override
    public void indexPersona(Long personaId) {
        PersonasEntity persona = personasRepository.findById(personaId).orElseThrow(() ->
                new NoSuchElementException("해당 페르소나는 존재 하지 않습니다."));

        deletePersonaIndex(personaId);

        TokenTextSplitter textSplitter = new TokenTextSplitter();
        List<Document> rawDoc = List.of(new Document(persona.getBackground()));
        List<Document> chunks = textSplitter.apply(rawDoc);

        List<Document> documents = chunks.stream()
                .map(chunk -> new Document(
                        chunk.getText(),
                        Map.of(
                                "sourceType", "PERSONA_BACKGROUND",
                                "sourceId", personaId.toString()
                        )
                )).collect(Collectors.toList());

        vectorStore.add(documents);
        log.info("페르소나 인덱스 등록 완료 personaId: {} 청크 수: {}", personaId, documents.size());
    }

    /**
     * 스토리 생성 (RAG + LLM 호출)
     * @param sessionId 세션 PK
     * @param userInput 유저 입력
     * @return LLM 응답 파싱 결과
     */
    @Transactional
    @Override
    public LlmTurnResult generateStory(Long sessionId, String userInput) {
        StorySessionsEntity storySession = storySessionsRepository.findById(sessionId).orElseThrow(() ->
                new NoSuchElementException("해당 세션은 찾을 수 없습니다."));

        WorldsEntity world = storySession.getWorlds();
        PersonasEntity persona = storySession.getPersonas();

        PersonasBaseStatsEntity personasBaseStat = personasBaseStatsRepository.findByPersonaId(persona.getId()).orElseThrow(() ->
                new NoSuchElementException("해당 페르소나의 스텟을 찾을 수 없습니다."));

        // 세계관 조회를 위한 메타데이터 필터
        FilterExpressionBuilder b = new FilterExpressionBuilder();
        Filter.Expression worldFilter = b.and(
                b.eq("sourceType", "WORLD_SETTING"),
                b.eq("sourceId", world.getId().toString())
        ).build();

        // 세계관 청크
        List<Document> worldDoc = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(userInput)
                        .topK(5)
                        .filterExpression(worldFilter)
                        .build()
        );

        // 페르소나 조회를 위한 메타데이터 필터
        Filter.Expression personaFilter = b.and(
                b.eq("sourceType", "PERSONA_BACKGROUND"),
                b.eq("sourceId", persona.getId().toString())
        ).build();

        List<Document> personaDoc = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(userInput)
                        .topK(3)
                        .filterExpression(personaFilter)
                        .build()
        );

        String worldContext = worldDoc.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n"));

        String personaContext = personaDoc.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n"));

        String systemPrompt = """
    당신은 %s 세계관의 스토리텔러입니다.
    
    [세계관 설정]
    %s
    
    [페르소나 정보]
    이름: %s
    배경: %s
    
    [현재 스탯]
    지혜: %d, 힘: %d, 민첩: %d, 카리스마: %d, 행운: %d
    
    [진행 현황]
    현재 턴: %d / %d
    
    [규칙]
    1. 소설 본문(story)은 2~4문단으로 작성하세요.
    2. 선택지(choices)는 반드시 3개를 제시하세요.
    3. 스탯 변화(statChanges)는 각 스탯별로 -2~+2 범위로 지정하세요.
    4. 아이템 이벤트(itemEvent)는 자연스러울 때만 생성하고 없으면 null입니다.
    5. 엔딩 조건(endingFlag)은 목표 턴에 근접하거나 중요한 사건 발생 시 true로 설정하세요.
    """.formatted(
                world.getName(), worldContext,
                persona.getName(), personaContext,
                personasBaseStat.getWisdom(), personasBaseStat.getStrength(),
                personasBaseStat.getAgility(), personasBaseStat.getCharisma(), personasBaseStat.getLuck(),
                storySession.getCurrentTurn(), world.getTargetTurn()
        );

        LlmTurnResult result = chatClient.prompt()
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, sessionId.toString()))
                .system(systemPrompt)
                .user(userInput)
                .call()
                .entity(LlmTurnResult.class);

        log.info("스토리: {}", result.getStory());
        log.info("선택지: {}", result.getChoices());
        log.info("스탯 변화: {}", result.getStatChanges());
        log.info("아이템 이벤트: {}", result.getItemEvent());
        log.info("엔딩 플래그: {}", result.isEndingFlag());

        return result;
    }
}
