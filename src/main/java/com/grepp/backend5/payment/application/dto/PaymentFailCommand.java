package com.grepp.backend5.payment.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 결제 실패 정보를 저장하기 위한 명령 DTO.
 */
@Schema(description = "결제 실패 저장 명령")
public record PaymentFailCommand(
        @Schema(description = "주문 ID")
        String orderId,
        @Schema(description = "토스 결제 키")
        String paymentKey,
        @Schema(description = "토스 오류 코드")
        String errorCode,
        @Schema(description = "토스 오류 메시지")
        String errorMessage,
        @Schema(description = "결제 금액")
        Long amount,
        @Schema(description = "원본 실패 페이로드(JSON 문자열)")
        String rawPayload
) {
}
