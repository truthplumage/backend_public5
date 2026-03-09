package com.grepp.backend5.kafka.presentation;

import com.grepp.backend5.kafka.application.OrderEventPublisher;
import com.grepp.backend5.kafka.dto.OrderDispatchResult;
import com.grepp.backend5.kafka.dto.OrderRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/kafka/orders")
@ConditionalOnProperty(prefix = "kafka", name = "enabled", havingValue = "true")
//@ConditionalOnProperty(prefix = "kafka", name = "enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class KafkaOrderController {

    private final OrderEventPublisher publisher;

    @PostMapping
    // HTTP로 주문 이벤트를 받아 카프카에 위임한다.
    public CompletableFuture<ResponseEntity<OrderDispatchResult>> publish(@Valid @RequestBody OrderRequest request) {
        return publisher.publish(request)
                .thenApply(result -> ResponseEntity.accepted().body(result));
    }
}
