package com.worldtaletalk.statistics.service;

import com.worldtaletalk.report.domain.ReportStatus;
import com.worldtaletalk.report.repository.ReportsRepository;
import com.worldtaletalk.session.domain.SessionStatus;
import com.worldtaletalk.session.repository.StorySessionsRepository;
import com.worldtaletalk.statistics.dto.AdminDashboardStatsDTO;
import com.worldtaletalk.statistics.dto.WorldStatsDTO;
import com.worldtaletalk.user.repository.UserRepository;
import com.worldtaletalk.world.repository.WorldsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminStatsServiceImpl implements AdminStatsService {
    private final UserRepository userRepository;
    private final WorldsRepository worldsRepository;
    private final StorySessionsRepository storySessionsRepository;
    private final ReportsRepository reportsRepository;

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
        return worldsRepository.findWorldStats().stream()
                .map(p -> new WorldStatsDTO(
                        p.getWorldId(),
                        p.getWorldName(),
                        p.getTotalSessions(),
                        p.getCompletedSessions(),
                        p.getAbandonedSessions(),
                        p.getRatingAvg()
                )).toList();
    }
}
