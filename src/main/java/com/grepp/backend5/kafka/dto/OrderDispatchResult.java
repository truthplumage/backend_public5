package com.grepp.backend5.kafka.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "비동기 주문 이벤트 발행 결과")
public record OrderDispatchResult(
        @Schema(description = "주문 ID")
        String orderId,
        @Schema(description = "발행된 Kafka 토픽명")
        String topic,
        @Schema(description = "저장된 파티션 번호")
        int partition,
        @Schema(description = "저장된 오프셋")
        long offset
) {
}
