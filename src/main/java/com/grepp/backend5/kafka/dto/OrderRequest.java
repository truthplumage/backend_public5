package com.grepp.backend5.kafka.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;

@Schema(description = "비동기 주문 이벤트 발행 요청")
public record OrderRequest(
        @Schema(description = "주문 ID")
        String orderId,
        @Schema(description = "주문자 회원 ID")
        String memberId,
        @Schema(description = "주문 총 금액")
        BigDecimal totalAmount,
        @Schema(description = "주문 상품 SKU 목록")
        List<String> itemSkus
) {
}
