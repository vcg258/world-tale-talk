package com.worldtaletalk.story.service;

import com.worldtaletalk.persona.repository.PersonasBaseStatsRepository;
import com.worldtaletalk.persona.repository.PersonasRepository;
import com.worldtaletalk.session.repository.StorySessionsRepository;
import com.worldtaletalk.story.dto.LlmTurnResult;
import com.worldtaletalk.world.repository.WorldsRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class StoryAiServiceImpl implements StoryAiService {
    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final WorldsRepository worldsRepository;
    private final PersonasRepository personasRepository;
    private final PersonasBaseStatsRepository personasBaseStatsRepository;
    private final StorySessionsRepository storySessionsRepository;

    public StoryAiServiceImpl(
            ChatClient.Builder builder,
            VectorStore vectorStore,
            WorldsRepository worldsRepository,
            PersonasRepository personasRepository,
            PersonasBaseStatsRepository personasBaseStatsRepository,
            StorySessionsRepository storySessionsRepository
    ) {
        this.chatClient = builder // TODO 세계관 프롬프트를 여기다가 넣는게 맞을까? 고민중
                .defaultSystem("""
                        일단 대기
                        """)
                .build();
        this.vectorStore = vectorStore;
        this.worldsRepository = worldsRepository;
        this.personasRepository = personasRepository;
        this.personasBaseStatsRepository = personasBaseStatsRepository;
        this.storySessionsRepository = storySessionsRepository;
    }

    /**
     * 세계관 등록/수정
     * @param worldId 세계관 PK
     */
    @Override
    public void indexWorld(Long worldId) {

    }

    /**
     * 세계관 삭제
     * @param worldId 세계관 PK
     */
    @Override
    public void deleteWorldIndex(Long worldId) {

    }

    /**
     * 페르소나 등록/수정
     * @param personaId 페르소나 PK
     */
    @Override
    public void indexPersona(Long personaId) {

    }

    /**
     * 페르소나 삭제
     * @param personaId 페르소나 PK
     */
    @Override
    public void deletePersonaIndex(Long personaId) {

    }

    /**
     * 스토리 생성 (RAG + LLM 호출)
     * @param sessionId 세션 PK
     * @param userInput 유저 입력
     * @return LLM 응답 파싱 결과
     */
    @Override
    public LlmTurnResult generateStory(Long sessionId, String userInput) {
        return null;
    }
}
