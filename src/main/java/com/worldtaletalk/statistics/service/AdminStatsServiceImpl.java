package com.worldtaletalk.statistics.service;

import com.worldtaletalk.report.domain.ReportStatus;
import com.worldtaletalk.report.repository.ReportsRepository;
import com.worldtaletalk.session.domain.SessionStatus;
import com.worldtaletalk.session.repository.StorySessionsRepository;
import com.worldtaletalk.statistics.dto.AdminDashboardStatsDTO;
import com.worldtaletalk.statistics.dto.WorldStatsDTO;
import com.worldtaletalk.user.repository.UserRepository;
import com.worldtaletalk.world.domain.DeletedWorldsEntity;
import com.worldtaletalk.world.repository.DeletedWorldsRepository;
import com.worldtaletalk.world.repository.WorldsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminStatsServiceImpl implements AdminStatsService {
    private final UserRepository userRepository;
    private final WorldsRepository worldsRepository;
    private final StorySessionsRepository storySessionsRepository;
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
}
