package com.grepp.backend5.batch.config;

import com.grepp.backend5.batch.job.SettlementTasklet;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class SettlementBatchConfig {

    public static final String SETTLEMENT_JOB_NAME = "settlementJob";
    public static final String SETTLEMENT_STEP_NAME = "settlementStep";

    @Bean
    public Job settlementJob(JobRepository jobRepository, Step settlementStep) {
        return new JobBuilder(SETTLEMENT_JOB_NAME, jobRepository)
                .start(settlementStep)
                .build();
    }

    @Bean
    public Step settlementStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            SettlementTasklet settlementTasklet
    ) {
        return new StepBuilder(SETTLEMENT_STEP_NAME, jobRepository)
                .tasklet(settlementTasklet, transactionManager)
                .build();
    }
}
