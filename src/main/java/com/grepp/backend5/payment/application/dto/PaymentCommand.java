package com.grepp.backend5.payment.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 결제 확인에 필요한 입력 데이터를 묶는 명령 DTO.
 */
@Schema(description = "결제 승인 명령")
public record PaymentCommand(
        @Schema(description = "토스 결제 키")
        String paymentKey,
        @Schema(description = "주문 ID")
        String orderId,
        @Schema(description = "결제 금액")
        Long amount
) {
}
