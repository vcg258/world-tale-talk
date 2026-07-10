package com.worldtaletalk.batch.job;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;


@Configuration
@RequiredArgsConstructor
public class WorldPersonaCleanupBatchConfig {

    private final WorldPersonaCleanupTasklet worldPersonaCleanupTasklet;

    /**
     * World/Persona 정리 Job
     *
     * @param jobRepository           Job 실행 이력 저장소
     * @param worldPersonaCleanupStep 단일 Tasklet 스텝
     * @return Job
     */
    @Bean
    public Job worldPersonaCleanupJob(JobRepository jobRepository, Step worldPersonaCleanupStep) {
        return new JobBuilder("worldPersonaCleanupJob", jobRepository)
                .start(worldPersonaCleanupStep)
                .build();
    }

    /**
     * World/Persona 정리 Step
     *
     * @param jobRepository      Job 실행 이력 저장소
     * @param transactionManager 트랜잭션 매니저
     * @return Step
     */
    @Bean
    public Step worldPersonaCleanupStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("worldPersonaCleanupStep", jobRepository)
                .tasklet(worldPersonaCleanupTasklet, transactionManager)
                .build();
    }
}
