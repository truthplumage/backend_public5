package com.grepp.backend5.order.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CreateOrderRequest(
        @Schema(description = "주문 번호(미입력 시 서버에서 자동 생성)")
        String orderNo,
        @Schema(description = "구매자 회원 ID(UUID)")
        UUID buyerId,
        @Schema(description = "판매자 회원 ID(UUID)")
        UUID sellerId,
        @Schema(description = "상품 ID(UUID)")
        UUID productId,
        @Schema(description = "주문 수량")
        Integer quantity,
        @Schema(description = "총 주문 금액")
        BigDecimal grossAmount,
        @Schema(description = "수수료 금액")
        BigDecimal feeAmount,
        @Schema(description = "환불 금액")
        BigDecimal refundAmount,
        @Schema(description = "주문 상태(PAID, CANCELED, REFUNDED 등)")
        String status,
        @Schema(description = "결제 완료 시각")
        LocalDateTime paidAt,
        @Schema(description = "요청 수행자 ID(UUID), 미입력 시 buyerId 또는 랜덤값 사용")
        UUID actorId
) {
}
