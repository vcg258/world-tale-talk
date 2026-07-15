package com.worldtaletalk.batch.job;

import com.worldtaletalk.batch.service.WorldPersonaPurgeJdbcService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * World/Persona 유예기간 경과 후 완전 삭제 배치 Tasklet
 * 청크 단위 처리가 필요 없는 단순 조회+삭제 로직이라 Tasklet 기반으로 구성함
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WorldPersonaCleanupTasklet implements Tasklet {

    private final static int GRACE_PERIOD_DAYS = 30;

    private final WorldPersonaPurgeJdbcService worldPersonaPurgeJdbcService;

    /**
     * 유예기간이 지난 세계관/페르소나를 조회해서 차례로 완전 삭제
     * 건별로 try/catch 처리해서 하나 실패해도 나머지 건은 계속 진행됨 (purgeWorld/purgePersona가 REQUIRES_NEW라 트랜잭션도 건별 분리돼있음)
     */
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(GRACE_PERIOD_DAYS);

        // 유예기간 지난 세계관 삭제
        List<Long> expiredWorldIds = worldPersonaPurgeJdbcService.findExpiredWorldIds(cutoff);
        for (Long worldId : expiredWorldIds) {
            try {
                worldPersonaPurgeJdbcService.purgeWorld(worldId);
            } catch (Exception e) {
                log.error("세계관 삭제 실패 worldId : {}", worldId, e);
            }
        }

        // 유예기간 지난 페르소나 삭제
        List<Long> expiredPersonaIds = worldPersonaPurgeJdbcService.findExpiredPersonaIds(cutoff);
        for (Long personaId : expiredPersonaIds) {
            try {
                worldPersonaPurgeJdbcService.purgePersona(personaId);
            } catch (Exception e) {
                log.error("페르소나 삭제 실패 personaId : {}", personaId, e);
            }
        }

        log.info("세계관 / 페르소나 삭제 완료 - 정리된 세계관 {}건, 페르소나 {}건", expiredWorldIds.size(), expiredPersonaIds.size());
        return RepeatStatus.FINISHED;
    }
}
