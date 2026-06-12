package com.worldtaletalk.stat.service;

import com.worldtaletalk.persona.domain.PersonasBaseStatsEntity;
import com.worldtaletalk.persona.repository.PersonasBaseStatsRepository;
import com.worldtaletalk.session.domain.StorySessionsEntity;
import com.worldtaletalk.session.repository.StorySessionsRepository;
import com.worldtaletalk.stat.mapper.SessionStatLogsMapper;
import com.worldtaletalk.stat.mapper.SessionStatSnapshotsMapper;
import com.worldtaletalk.stat.domain.SessionStatLogsEntity;
import com.worldtaletalk.stat.domain.SessionStatSnapshotsEntity;
import com.worldtaletalk.stat.dto.SessionStatLogsDTO;
import com.worldtaletalk.stat.dto.SessionStatSnapshotsDTO;
import com.worldtaletalk.stat.repository.SessionStatLogsRepository;
import com.worldtaletalk.stat.repository.SessionStatSnapshotsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Log4j2
@Service
@RequiredArgsConstructor
public class SessionStatsServiceImpl implements SessionStatsService {
    private final SessionStatLogsRepository sessionStatLogsRepository;
    private final SessionStatSnapshotsRepository sessionStatSnapshotsRepository;
    private final StorySessionsRepository storySessionsRepository;
    private final PersonasBaseStatsRepository personasBaseStatsRepository;
    private final SessionStatLogsMapper sessionStatLogsMapper;
    private final SessionStatSnapshotsMapper sessionStatSnapshotsMapper;

    /**
     * 스텟 변화 로그 생성
     * @param sessionStatLogsDTO 턴별 스텟 변화량 정보
     * @return 저장된 스텟 로그
     */
    @Transactional
    @Override
    public SessionStatLogsDTO addStatLog(SessionStatLogsDTO sessionStatLogsDTO) {
        StorySessionsEntity storySession = storySessionsRepository.findById(sessionStatLogsDTO.getStorySessionId()).orElseThrow(() ->
                new NoSuchElementException("해당 세션은 존재하지 않습니다."));

        SessionStatLogsEntity sessionStatLog = SessionStatLogsEntity.createSessionStatLog(
                sessionStatLogsDTO.getTurn(),
                sessionStatLogsDTO.getWisdomDelta(),
                sessionStatLogsDTO.getStrengthDelta(),
                sessionStatLogsDTO.getAgilityDelta(),
                sessionStatLogsDTO.getCharismaDelta(),
                sessionStatLogsDTO.getLuckDelta(),
                storySession
        );

        SessionStatLogsEntity save = sessionStatLogsRepository.save(sessionStatLog);
        log.info("스텟 로그 생성 : {}", save);

        return sessionStatLogsMapper.toDTO(save);
    }

    /**
     * 현재 스텟 합산 조회 (기본 스텟 + 모든 로그 변화량 누적)
     * @param sessionId 세션 PK
     * @return 스텟별 합산 값 (wisdom, strength, agility, charisma, luck)
     */
    @Transactional(readOnly = true)
    @Override
    public Map<String, Integer> getCurrentStats(Long sessionId) {
        StorySessionsEntity storySession = storySessionsRepository.findById(sessionId).orElseThrow(() ->
                new NoSuchElementException("해당 세션은 존재하지 않습니다."));

        // 세션의 페르소나 기본 스텟 조회
        PersonasBaseStatsEntity baseStats = personasBaseStatsRepository.findByPersonaId(storySession.getPersonas().getId()).orElseThrow(() ->
                new NoSuchElementException("해당 페르소나의 스텟이 존재하지 않습니다."));

        // 세션의 전체 스텟 로그 조회 후 변화량 누적 합산
        List<SessionStatLogsEntity> statLogs = sessionStatLogsRepository.findAllByStorySession_Id(sessionId);

        return getStringIntegerMap(baseStats, statLogs);
    }

    private Map<String, Integer> getStringIntegerMap(PersonasBaseStatsEntity baseStats, List<SessionStatLogsEntity> statLogs) {
        int wisdom = baseStats.getWisdom();
        int strength = baseStats.getStrength();
        int agility = baseStats.getAgility();
        int charisma = baseStats.getCharisma();
        int luck = baseStats.getLuck();

        for (SessionStatLogsEntity statLog : statLogs) {
            wisdom += statLog.getWisdomDelta();
            strength += statLog.getStrengthDelta();
            agility += statLog.getAgilityDelta();
            charisma += statLog.getCharismaDelta();
            luck += statLog.getLuckDelta();
        }

        Map<String, Integer> currentStats = new HashMap<>();
        currentStats.put("wisdom", wisdom);
        currentStats.put("strength", strength);
        currentStats.put("agility", agility);
        currentStats.put("charisma", charisma);
        currentStats.put("luck", luck);
        return currentStats;
    }

    /**
     * 엔딩 시점 스텟 스냅샷 생성
     * @param sessionId 세션 PK
     * @return 저장된 스냅샷
     */
    @Transactional
    @Override
    public SessionStatSnapshotsDTO addSnapshot(Long sessionId) {
        StorySessionsEntity storySession = storySessionsRepository.findById(sessionId).orElseThrow(() ->
                new NoSuchElementException("해당 세션은 존재하지 않습니다."));

        // 현재 합산 스텟 기준으로 스냅샷 생성
        Map<String, Integer> currentStats = getCurrentStats(sessionId);

        SessionStatSnapshotsEntity snapshot = SessionStatSnapshotsEntity.createSessionStatSnapshots(
                currentStats.get("wisdom"),
                currentStats.get("strength"),
                currentStats.get("agility"),
                currentStats.get("charisma"),
                currentStats.get("luck"),
                storySession
                );

        SessionStatSnapshotsEntity save = sessionStatSnapshotsRepository.save(snapshot);
        log.info("엔딩 스냅샷 생성 : {}", save);

        return sessionStatSnapshotsMapper.toDTO(save);
    }

    /**
     * 스텟 로그 삭제 (LLM호출 실패시 보상용)
     *
     * @param statLogId 스텟 로그 PK
     */
    @Transactional
    @Override
    public void deleteStatLog(Long statLogId) {
        sessionStatLogsRepository.deleteById(statLogId);
        log.info("스텟 로그 삭제 statLogId : {}", statLogId);
    }
}
