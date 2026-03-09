package com.grepp.backend5.order.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "주문 응답")
public record OrderResponse(
        @Schema(description = "주문 ID(UUID)")
        UUID id,
        @Schema(description = "주문 번호")
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
        @Schema(description = "순 정산 금액(gross - fee - refund)")
        BigDecimal netAmount,
        @Schema(description = "주문 상태")
        String status,
        @Schema(description = "결제 완료 시각")
        LocalDateTime paidAt,
        @Schema(description = "정산 완료 여부")
        Boolean settled,
        @Schema(description = "정산 배치 ID(UUID)")
        UUID settlementBatchId,
        @Schema(description = "생성자 ID(UUID)")
        UUID regId,
        @Schema(description = "생성 일시")
        LocalDateTime regDt,
        @Schema(description = "수정자 ID(UUID)")
        UUID modifyId,
        @Schema(description = "수정 일시")
        LocalDateTime modifyDt
) {
}
