package com.grepp.backend5.batch.service;

import com.grepp.backend5.batch.config.SettlementBatchConfig;
import java.time.LocalDate;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class SettlementJobLauncher {

    private final JobOperator jobOperator;
    private final Job settlementTaskletJob;
    private final Job settlementChunkJob;

    public SettlementJobLauncher(
            JobOperator jobOperator,
            @Qualifier(SettlementBatchConfig.SETTLEMENT_JOB_NAME) Job settlementTaskletJob,
            @Qualifier(SettlementBatchConfig.SETTLEMENT_CHUNK_JOB_NAME) Job settlementChunkJob
    ) {
        this.jobOperator = jobOperator;
        this.settlementTaskletJob = settlementTaskletJob;
        this.settlementChunkJob = settlementChunkJob;
    }

    public JobExecution launch(LocalDate settlementDate) throws Exception {
        return launchTasklet(settlementDate);
    }

    public JobExecution launchTasklet(LocalDate settlementDate) throws Exception {
        return jobOperator.start(settlementTaskletJob, buildJobParameters(settlementDate, "tasklet"));
    }

    public JobExecution launchChunk(LocalDate settlementDate) throws Exception {
        return jobOperator.start(settlementChunkJob, buildJobParameters(settlementDate, "chunk"));
    }

    private JobParameters buildJobParameters(LocalDate settlementDate, String mode) {
        return new JobParametersBuilder()
                .addString("settlementDate", settlementDate.toString())
                .addString("mode", mode)
                // 동일 날짜 재실행을 위한 유니크 파라미터
                .addLong("requestedAt", System.currentTimeMillis())
                .toJobParameters();
    }
}
