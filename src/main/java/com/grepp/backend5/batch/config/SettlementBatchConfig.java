package com.grepp.backend5.batch.config;

import com.grepp.backend5.batch.job.SettlementTasklet;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class SettlementBatchConfig {

    private static final Logger log = LoggerFactory.getLogger(SettlementBatchConfig.class);

    public static final String SETTLEMENT_JOB_NAME = "settlementJob";
    public static final String SETTLEMENT_STEP_NAME = "settlementStep";
    public static final String SETTLEMENT_CHUNK_JOB_NAME = "settlementChunkJob";
    public static final String SETTLEMENT_CHUNK_STEP_NAME = "settlementChunkStep";

    @Bean
    public Job settlementJob(JobRepository jobRepository, Step settlementStep) {
        return new JobBuilder(SETTLEMENT_JOB_NAME, jobRepository)
                .start(settlementStep)
                .build();
    }

    @Bean
    public Job settlementChunkJob(JobRepository jobRepository, Step settlementChunkStep) {
        return new JobBuilder(SETTLEMENT_CHUNK_JOB_NAME, jobRepository)
                .start(settlementChunkStep)
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

    @Bean
    public Step settlementChunkStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ItemReader<Integer> settlementChunkReader,
            ItemProcessor<Integer, String> settlementChunkProcessor,
            ItemWriter<String> settlementChunkWriter
    ) {
        return new StepBuilder(SETTLEMENT_CHUNK_STEP_NAME, jobRepository)
                .<Integer, String>chunk(3)
                .transactionManager(transactionManager)
                .reader(settlementChunkReader)
                .processor(settlementChunkProcessor)
                .writer(settlementChunkWriter)
                .build();
    }

    @Bean
    public ItemReader<Integer> settlementChunkReader() {
        return new ListItemReader<>(List.of(1001, 1002, 1003, 1004, 1005));
    }

    @Bean
    public ItemProcessor<Integer, String> settlementChunkProcessor() {
        return orderId -> "SETTLEMENT_LINE(orderId=" + orderId + ", date=" + LocalDate.now() + ")";
    }

    @Bean
    public ItemWriter<String> settlementChunkWriter() {
        return chunk -> chunk.forEach(line -> log.info("Chunk writer persisted: {}", line));
    }
}
