package com.grepp.backend5.kafka.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Schema(description = "비동기 주문 이벤트 메시지")
public record OrderEvent(
        @Schema(description = "주문 ID")
        String orderId,
        @Schema(description = "주문자 회원 ID")
        String memberId,
        @Schema(description = "주문 총 금액")
        BigDecimal totalAmount,
        @Schema(description = "상품 목록 코드(Stock Keeping Unit)")
        List<String> itemSkus,
        @Schema(description = "이벤트 생성 시각(UTC)")
        Instant createdAt
) {
}
