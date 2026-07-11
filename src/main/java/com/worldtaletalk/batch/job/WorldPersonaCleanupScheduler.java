package com.worldtaletalk.batch.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;


@Slf4j
@Component
@RequiredArgsConstructor
public class WorldPersonaCleanupScheduler {

    private final JobLauncher jobLauncher;
    private final Job worldPersonaCleanupJob;

    @Scheduled(cron = "0 30 0 * * *") // TODO 필요하면 수정
    public void run() {
        // 매 실행마다 파라미터를 다르게 줘야 동일 JobInstance로 취급되지 않음
        JobParameters jobParameters = new JobParametersBuilder()
                .addLocalDateTime("executedAt", LocalDateTime.now())
                .toJobParameters();

        try {
            jobLauncher.run(worldPersonaCleanupJob, jobParameters);
        } catch (Exception e) {
            log.error("배치 실행 실패", e);
        }
    }
}
