package com.worldtaletalk.story.service;

import com.worldtaletalk.inventory.dto.SessionItemsDTO;
import com.worldtaletalk.inventory.service.SessionItemsService;
import com.worldtaletalk.persona.domain.PersonasEntity;
import com.worldtaletalk.session.domain.StorySessionsEntity;
import com.worldtaletalk.session.repository.StorySessionsRepository;
import com.worldtaletalk.stat.service.SessionStatsService;
import com.worldtaletalk.story.dto.LlmTurnResult;
import com.worldtaletalk.world.domain.WorldsEntity;
import com.worldtaletalk.world.repository.WorldsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoryAiServiceImpl implements StoryAiService {
    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final WorldsRepository worldsRepository;
    private final StorySessionsRepository storySessionsRepository;
    private final SessionStatsService sessionStatsService;
    private final SessionItemsService sessionItemsService;

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
     * 스토리 생성 (RAG + LLM 호출)
     * @param sessionId 세션 PK
     * @param userInput 유저 입력
     * @return LLM 응답 파싱 결과
     */
    @Override
    public LlmTurnResult generateStory(Long sessionId, String userInput) {
        StorySessionsEntity storySession = storySessionsRepository.findByIdWithWorldAndPersonas(sessionId).orElseThrow(() ->
                new NoSuchElementException("해당 세션은 찾을 수 없습니다."));
        // 인벤토리 조회
        List<SessionItemsDTO> inventory = sessionItemsService.getInventory(storySession.getId());

        WorldsEntity world = storySession.getWorlds();
        PersonasEntity persona = storySession.getPersonas();

        // 현재 스텟을 조회 (현재 스텟으로 인해 LLM이 스토리 진행을 하기 위함)
        Map<String, Integer> currentStats = sessionStatsService.getCurrentStats(sessionId);

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

        String worldContext = worldDoc.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n"));

        // 페르소나 배경
        String personaContext = persona.getBackground();

        // 인벤토리
        String inventoryContext = inventory.isEmpty() ?
                "보유중인 아이템 없음" : inventory.stream().map(item ->
                    "%s (%s등급) - %s".formatted(
                            item.getItemName(),
                            item.getGrade(),
                            item.getItemDescription()))
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
               
               [보유 아이템]
               %s
               
               [규칙]
               1. 소설 본문(story)은 2~4문단으로 작성하세요.
               2. 선택지(choices)는 반드시 3개를 제시하세요.
               3. statChanges 에는 유저의 이번 행동에 대한 스탯 변화를 항상 담으세요.
                  변화가 없다고 판단되면 빈 객체 {} 로 반환하세요.
               4. statChangesByChoice 에는 선택지 번호별 스탯 변화를 담으세요.
                  키는 반드시 choice1, choice2, choice3 입니다.
               5. 두 필드의 스탯 키는 반드시 wisdom, strength, agility, charisma, luck 만 사용하고
                  각 값은 -2에서 +2 범위입니다.
               6. statChanges 와 statChangesByChoice 는 항상 두 필드 모두 반환하세요.
                  해당 없으면 빈 객체 {} 로 반환하세요.
               7. 아이템 이벤트(itemEvent)는 자연스러울 때만 생성하고 없으면 null입니다.
               8. 엔딩 조건(endingFlag)은 목표 턴에 근접하거나 중요한 사건 발생 시 true로 설정하세요.
               9. 현재 스탯 수치를 스토리 전개에 반영하세요.
                  높은 스탯은 해당 능력이 빛나는 전개로, 낮은 스탯은 약점이 드러나는 전개로 표현하세요.
                  예를 들어 행운이 높으면 우연한 발견이나 기회가 찾아오고
                  힘이 낮으면 힘으로 해결하기 어려운 상황이 됩니다.
               10. 보유 아이템은 스토리 전개에 자연스럽게 반영하세요.
                   아이템을 잃는 이벤트(itemEvent LOSE)는 반드시 보유 중인 아이템 이름을 사용하세요.
               """.formatted(
                world.getName(), worldContext, // 세계관 이름, 설정
                persona.getName(), personaContext, // 페르소나 이름, 배경
                currentStats.get("wisdom"), currentStats.get("strength"),
                currentStats.get("agility"), currentStats.get("charisma"), currentStats.get("luck"),
                storySession.getCurrentTurn(), world.getTargetTurn(),
                inventoryContext
        );

        LlmTurnResult result = chatClient.prompt()
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, sessionId.toString()))
                .system(systemPrompt)
                .user(userInput)
                .call()
                .entity(LlmTurnResult.class);

        log.info("스토리: {}", result.getStory());
        log.info("선택지: {}", result.getChoices());
        log.info("자유 입력 스탯 변화: {}", result.getStatChanges());
        log.info("선택지별 스탯 변화: {}", result.getStatChangesByChoice());
        log.info("아이템 이벤트: {}", result.getItemEvent());
        log.info("엔딩 플래그: {}", result.isEndingFlag());

        return result;
    }
}
