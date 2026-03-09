package com.grepp.backend5.kafka.application;

import com.grepp.backend5.kafka.dto.OrderEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
//@ConditionalOnProperty(prefix = "kafka", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnProperty(prefix = "kafka", name = "enabled", havingValue = "true")
@Slf4j
public class OrderEventListener {

//    private final ExecutorService workerPool = Executors.newFixedThreadPool(4);

    // 주문 토픽을 구독해 브로커에서 메시지가 들어오면 비동기로 처리한다.
    @KafkaListener(
            topics = "${kafka.topic.async-orders:async-orders}",
            groupId = "${kafka.consumer.group-id:async-sample-group}",
            containerFactory = "asyncOrderKafkaListenerContainerFactory"
    )
    public void handle(OrderEvent event) {
        log.info("handle {}", event.orderId());
        simulateComplexOperation(event);
//        CompletableFuture
//                .runAsync(() -> simulateComplexOperation(event), workerPool)
//                .exceptionally(ex -> {
//                    log.error("Async processing failed for order {}", event.orderId(), ex);
//                    return null;
//                });
    }

    private void simulateComplexOperation(OrderEvent event) {
        // 샘플을 위해 1초간 지연을 주어 비즈니스 로직이 길다는 상황을 표현한다.
        try {
            Thread.sleep(Duration.ofSeconds(1).toMillis());
            log.info("Completed async pipeline for order {} with {} items", event.orderId(), event.itemSkus().size());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while handling async order event", e);
        }
    }

//    @PreDestroy
//    public void shutdownWorkerPool() {
//        workerPool.shutdown();
//    }
}
