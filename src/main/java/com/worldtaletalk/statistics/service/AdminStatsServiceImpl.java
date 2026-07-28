package com.worldtaletalk.statistics.service;

import com.worldtaletalk.report.domain.ReportStatus;
import com.worldtaletalk.chat.domain.ChatRole;
import com.worldtaletalk.chat.repository.ChatMessagesRepository;
import com.worldtaletalk.report.repository.ReportsRepository;
import com.worldtaletalk.session.domain.SessionStatus;
import com.worldtaletalk.session.repository.StorySessionsRepository;
import com.worldtaletalk.statistics.dto.AdminDashboardStatsDTO;
import com.worldtaletalk.statistics.dto.AdminDetailedStatsDTO;
import com.worldtaletalk.statistics.dto.UsageStatsItemDTO;
import com.worldtaletalk.statistics.dto.TimeSeriesStatsDTO;
import com.worldtaletalk.statistics.dto.WorldStatsDTO;
import com.worldtaletalk.statistics.projection.SessionLastChatProjection;
import com.worldtaletalk.user.repository.UserRepository;
import com.worldtaletalk.world.domain.DeletedWorldsEntity;
import com.worldtaletalk.world.repository.DeletedWorldsRepository;
import com.worldtaletalk.world.repository.WorldsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminStatsServiceImpl implements AdminStatsService {
    private final UserRepository userRepository;
    private final WorldsRepository worldsRepository;
    private final StorySessionsRepository storySessionsRepository;
    private final ChatMessagesRepository chatMessagesRepository;
    private final ReportsRepository reportsRepository;
    private final DeletedWorldsRepository deletedWorldsRepository;

    /**
     * 어드민 대시보드 요약 통계 조회
     *
     * @return 전체 회원 수, 세계관 수, 진행 중 세션 수, 처리 대기 신고 수
     */
    @Override
    public AdminDashboardStatsDTO getDashboardStats() {
        return AdminDashboardStatsDTO.builder()
                .userCount(userRepository.count())
                .worldCount(worldsRepository.count())
                .pendingReportCount(reportsRepository.countByStatus(ReportStatus.PENDING))
                .activeSessionCount(storySessionsRepository.countByStatus(SessionStatus.IN_PROGRESS))
                .build();
    }

    /**
     * 세계관별 통계 목록 조회 (어드민용)
     *
     * @return 세계관별 플레이 수, 완결/포기 수, 평균 별점
     */
    @Override
    public List<WorldStatsDTO> getWorldStats() {
        // 삭제 안된 세계관 통계
        List<WorldStatsDTO> activeStats = worldsRepository.findWorldStats().stream()
                .map(p -> new WorldStatsDTO(
                        p.getWorldId(),
                        p.getWorldName(),
                        p.getTotalSessions(),
                        p.getCompletedSessions(),
                        p.getAbandonedSessions(),
                        p.getRatingAvg(),
                        false
                )).toList();

        // 삭제 되어 스냅샷되어있는 세계관 통계
        List<WorldStatsDTO> deletedStats = deletedWorldsRepository.findAll().stream()
                .map(d -> new WorldStatsDTO(
                        null,
                        d.getName(),
                        (long) d.getPlayCount(),
                        (long) d.getCompletedCount(),
                        (long) d.getAbandonedCount(),
                        d.getRatingAvg(),
                        true
                )).toList();

        return Stream.concat(activeStats.stream(), deletedStats.stream())
                .sorted(Comparator.comparing(WorldStatsDTO::getTotalSessions, Comparator.reverseOrder()))
                .toList();
    }
    /**
     * 기간별 상세 통계 조회
     *
     * @param startDate 조회 시작일
     * @param endDate 조회 종료일
     * @return 기간별 이야기/세계관/페르소나 통계
     */
    @Override
    public AdminDetailedStatsDTO getDetailedStats(LocalDate startDate, LocalDate endDate) {
        LocalDate safeStart = startDate != null ? startDate : LocalDate.now();
        LocalDate safeEnd = endDate != null ? endDate : safeStart;
        if (safeEnd.isBefore(safeStart)) {
            LocalDate temp = safeStart;
            safeStart = safeEnd;
            safeEnd = temp;
        }

        LocalDateTime startAt = safeStart.atStartOfDay();
        LocalDateTime endExclusive = safeEnd.plusDays(1).atStartOfDay();
        List<com.worldtaletalk.session.domain.StorySessionsEntity> sessions =
                storySessionsRepository.findAllForStatsBetween(startAt, endExclusive);
        Map<Long, LocalDateTime> lastChatBySession = getLastChatBySession(sessions);

        LocalDateTime abandonedCutoff = LocalDate.now().minusDays(8).atStartOfDay();
        LocalDateTime activeCutoff = LocalDate.now().minusDays(7).atStartOfDay();

        long endingCount = sessions.stream()
                .filter(this::hasReachedEnding)
                .count();
        long abandonedCount = sessions.stream()
                .filter(session -> isAbandoned(session, lastChatBySession.get(session.getId()), abandonedCutoff))
                .count();
        long activeCount = sessions.stream()
                .filter(session -> isActive(session, lastChatBySession.get(session.getId()), activeCutoff))
                .count();

        List<UsageStatsItemDTO> worldUsage = buildWorldUsage(sessions);
        List<UsageStatsItemDTO> personaUsage = buildPersonaUsage(sessions);
        List<TimeSeriesStatsDTO> timeSeries = buildTimeSeries(sessions, safeStart, safeEnd, lastChatBySession, abandonedCutoff, activeCutoff);
        List<UsageStatsItemDTO> interestTargets = Stream.concat(
                        worldUsage.stream().map(item -> new UsageStatsItemDTO("세계관: " + item.getLabel(), item.getCount(), item.getRatio())),
                        personaUsage.stream().map(item -> new UsageStatsItemDTO("페르소나: " + item.getLabel(), item.getCount(), item.getRatio()))
                )
                .sorted(Comparator.comparingLong(UsageStatsItemDTO::getCount).reversed())
                .limit(8)
                .toList();

        return AdminDetailedStatsDTO.builder()
                .startDate(safeStart)
                .endDate(safeEnd)
                .totalStories(sessions.size())
                .endingCount(endingCount)
                .abandonedCount(abandonedCount)
                .activeCount(activeCount)
                .worldUsage(worldUsage)
                .personaUsage(personaUsage)
                .interestTargets(interestTargets)
                .timeSeries(timeSeries)
                .build();
    }

    private Map<Long, LocalDateTime> getLastChatBySession(List<com.worldtaletalk.session.domain.StorySessionsEntity> sessions) {
        if (sessions.isEmpty()) {
            return Map.of();
        }
        List<Long> sessionIds = sessions.stream()
                .map(com.worldtaletalk.session.domain.StorySessionsEntity::getId)
                .toList();
        return chatMessagesRepository.findLastUserChatsBySessionIds(sessionIds, ChatRole.USER).stream()
                .collect(Collectors.toMap(
                        SessionLastChatProjection::getSessionId,
                        SessionLastChatProjection::getLastChatAt
                ));
    }

    private boolean hasReachedEnding(com.worldtaletalk.session.domain.StorySessionsEntity session) {
        return session.getStatus() == SessionStatus.ENDING
                || session.getStatus() == SessionStatus.EPILOGUE
                || session.getStatus() == SessionStatus.COMPLETED;
    }

    private boolean isAbandoned(
            com.worldtaletalk.session.domain.StorySessionsEntity session,
            LocalDateTime lastChatAt,
            LocalDateTime abandonedCutoff
    ) {
        if (session.getStatus() == SessionStatus.ABANDONED || session.isHiddenByUser()) {
            return true;
        }
        LocalDateTime activityAt = lastChatAt != null ? lastChatAt : session.getCreatedAt();
        return session.getStatus() == SessionStatus.IN_PROGRESS && activityAt.isBefore(abandonedCutoff);
    }

    private boolean isActive(
            com.worldtaletalk.session.domain.StorySessionsEntity session,
            LocalDateTime lastChatAt,
            LocalDateTime activeCutoff
    ) {
        if (session.getStatus() != SessionStatus.IN_PROGRESS || session.isHiddenByUser()) {
            return false;
        }
        LocalDateTime activityAt = lastChatAt != null ? lastChatAt : session.getCreatedAt();
        return !activityAt.isBefore(activeCutoff);
    }

    private List<UsageStatsItemDTO> buildWorldUsage(List<com.worldtaletalk.session.domain.StorySessionsEntity> sessions) {
        return buildUsageStats(
                sessions,
                session -> session.getWorlds().getName(),
                session -> session.getWorlds().getName()
        );
    }

    private List<UsageStatsItemDTO> buildPersonaUsage(List<com.worldtaletalk.session.domain.StorySessionsEntity> sessions) {
        return buildUsageStats(
                sessions,
                session -> session.getPersonas().getName(),
                session -> session.getPersonas().getWorld().getName()
        );
    }

    private List<UsageStatsItemDTO> buildUsageStats(
            List<com.worldtaletalk.session.domain.StorySessionsEntity> sessions,
            Function<com.worldtaletalk.session.domain.StorySessionsEntity, String> labelExtractor,
            Function<com.worldtaletalk.session.domain.StorySessionsEntity, String> groupExtractor
    ) {
        int total = sessions.size();
        if (total == 0) {
            return List.of();
        }
        Map<String, Long> counts = sessions.stream()
                .map(labelExtractor)
                .collect(Collectors.groupingBy(label -> label, LinkedHashMap::new, Collectors.counting()));
        Map<String, String> groups = sessions.stream()
                .collect(Collectors.toMap(
                        labelExtractor,
                        groupExtractor,
                        (first, ignored) -> first,
                        LinkedHashMap::new
                ));
        return counts.entrySet().stream()
                .map(entry -> new UsageStatsItemDTO(
                        entry.getKey(),
                        entry.getValue(),
                        Math.round(entry.getValue() * 1000.0 / total) / 10.0,
                        groups.get(entry.getKey())
                ))
                .sorted(Comparator.comparingLong(UsageStatsItemDTO::getCount).reversed())
                .toList();
    }

    private List<TimeSeriesStatsDTO> buildTimeSeries(
            List<com.worldtaletalk.session.domain.StorySessionsEntity> sessions,
            LocalDate startDate,
            LocalDate endDate,
            Map<Long, LocalDateTime> lastChatBySession,
            LocalDateTime abandonedCutoff,
            LocalDateTime activeCutoff
    ) {
        Map<LocalDate, List<com.worldtaletalk.session.domain.StorySessionsEntity>> sessionsByDate = sessions.stream()
                .collect(Collectors.groupingBy(session -> session.getCreatedAt().toLocalDate()));
        return startDate.datesUntil(endDate.plusDays(1))
                .map(date -> {
                    List<com.worldtaletalk.session.domain.StorySessionsEntity> dailySessions =
                            sessionsByDate.getOrDefault(date, List.of());
                    long dailyEndingCount = dailySessions.stream()
                            .filter(this::hasReachedEnding)
                            .count();
                    long dailyAbandonedCount = dailySessions.stream()
                            .filter(session -> isAbandoned(session, lastChatBySession.get(session.getId()), abandonedCutoff))
                            .count();
                    long dailyActiveCount = dailySessions.stream()
                            .filter(session -> isActive(session, lastChatBySession.get(session.getId()), activeCutoff))
                            .count();
                    return new TimeSeriesStatsDTO(
                            date,
                            dailySessions.size(),
                            dailyEndingCount,
                            dailyAbandonedCount,
                            dailyActiveCount
                    );
                })
                .toList();
    }

}
