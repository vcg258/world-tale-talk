package com.worldtaletalk.session.scheduler;

import com.worldtaletalk.session.domain.SessionStatus;
import com.worldtaletalk.session.domain.StorySessionsEntity;
import com.worldtaletalk.session.repository.StorySessionsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 중도이탈 자동 처리 스케줄러
 * 유저가 포기 버튼 없이 이탈해서 N일 이상 갱신 없는 IN_PROGRESS 세션을 ABANDONED로 자동 전환
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AbandonedSessionCleanupScheduler {
    private final static int INACTIVITY_DAYS = 3; // TODO 필요하면 수정
    private final StorySessionsRepository storySessionsRepository;

    /**
     * 매일 자정 실행
     */
    @Transactional
    @Scheduled(cron = "0 20 0 * * *")
    public void run() {
        try {
            LocalDateTime cutoff = LocalDateTime.now().minusDays(INACTIVITY_DAYS);
            // 기준일 이전까지 갱신 없는 진행중 세션 조회
            List<StorySessionsEntity> before =
                    storySessionsRepository.findAllByStatusAndUpdatedAtBefore(SessionStatus.IN_PROGRESS, cutoff);

            // 도메인 메서드로 상태 변경 후 일괄 저장
            for (StorySessionsEntity session : before) {
                session.changeInformationByStorySession(SessionStatus.ABANDONED);
            }
            storySessionsRepository.saveAll(before);
            log.info("중도이탈 세션 자동 정리 완료 - {}건", before.size());
        } catch (Exception e) {
            log.error("중도이탈 세션 자동 정리 실패", e);
        }
    }
}
