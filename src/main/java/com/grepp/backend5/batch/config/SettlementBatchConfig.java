package com.grepp.backend5.batch.config;

import com.grepp.backend5.batch.job.SettlementTasklet;
import com.grepp.backend5.order.domain.model.Order;
import com.grepp.backend5.order.domain.repository.OrderRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Value;
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
            ItemReader<Order> settlementChunkReader,
            ItemProcessor<Order, SettlementLine> settlementChunkProcessor,
            ItemWriter<SettlementLine> settlementChunkWriter
    ) {
        return new StepBuilder(SETTLEMENT_CHUNK_STEP_NAME, jobRepository)
                .<Order, SettlementLine>chunk(100)
                .transactionManager(transactionManager)
                .reader(settlementChunkReader)
                .processor(settlementChunkProcessor)
                .writer(settlementChunkWriter)
                .build();
    }

    @Bean
    @StepScope
    public ListItemReader<Order> settlementChunkReader(
            OrderRepository orderRepository,
            @Value("#{jobParameters['settlementDate']}") String settlementDateRaw
    ) {
        LocalDate settlementDate = parseSettlementDate(settlementDateRaw);
        LocalDateTime fromInclusive = settlementDate.atStartOfDay();
        LocalDateTime toExclusive = fromInclusive.plusDays(1);

        List<Order> orders = orderRepository.findUnsettledPaidOrders(fromInclusive, toExclusive);
        log.info("Chunk reader loaded {} orders for settlementDate={}", orders.size(), settlementDate);
        return new ListItemReader<>(orders);
    }

    @Bean
    public ItemProcessor<Order, SettlementLine> settlementChunkProcessor() {
        return order -> new SettlementLine(
                order.getId(),
                order.getOrderNo(),
                order.getSellerId(),
                order.getGrossAmount(),
                order.getFeeAmount(),
                order.getRefundAmount(),
                order.getNetAmount(),
                order.getPaidAt()
        );
    }

    @Bean
    public ItemWriter<SettlementLine> settlementChunkWriter() {
        return chunk -> chunk.forEach(line ->
                log.info(
                        "Chunk writer processed orderId={}, orderNo={}, sellerId={}, gross={}, fee={}, refund={}, net={}, paidAt={}",
                        line.orderId(),
                        line.orderNo(),
                        line.sellerId(),
                        line.grossAmount(),
                        line.feeAmount(),
                        line.refundAmount(),
                        line.netAmount(),
                        line.paidAt()
                )
        );
    }

    private LocalDate parseSettlementDate(String settlementDateRaw) {
        if (settlementDateRaw == null || settlementDateRaw.isBlank()) {
            return LocalDate.now();
        }
        return LocalDate.parse(settlementDateRaw);
    }

    public record SettlementLine(
            UUID orderId,
            String orderNo,
            UUID sellerId,
            java.math.BigDecimal grossAmount,
            java.math.BigDecimal feeAmount,
            java.math.BigDecimal refundAmount,
            java.math.BigDecimal netAmount,
            LocalDateTime paidAt
    ) {
    }
}
