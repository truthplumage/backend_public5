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
    private final Job settlementJob;

    public SettlementJobLauncher(
            JobOperator jobOperator,
            @Qualifier(SettlementBatchConfig.SETTLEMENT_JOB_NAME) Job settlementJob
    ) {
        this.jobOperator = jobOperator;
        this.settlementJob = settlementJob;
    }

    public JobExecution launch(LocalDate settlementDate) throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("settlementDate", settlementDate.toString())
                // 동일 날짜 재실행을 위한 유니크 파라미터
                .addLong("requestedAt", System.currentTimeMillis())
                .toJobParameters();

        return jobOperator.start(settlementJob, jobParameters);
    }
}
