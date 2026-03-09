package com.grepp.backend5.payment.presentation.dto;


import com.grepp.backend5.payment.application.dto.PaymentFailCommand;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 결제 실패 콜백을 받기 위한 요청 DTO.
 */
@Schema(description = "결제 실패 저장 요청")
public record PaymentFailRequest(
        @Schema(description = "주문 ID")
        String orderId,
        @Schema(description = "토스 결제 키")
        String paymentKey,
        @Schema(description = "토스 오류 코드")
        String code,
        @Schema(description = "토스 오류 메시지")
        String message,
        @Schema(description = "결제 금액")
        Long amount,
        @Schema(description = "원본 실패 페이로드(JSON 문자열)")
        String rawPayload
) {

    public PaymentFailCommand toCommand() {
        return new PaymentFailCommand(orderId, paymentKey, code, message, amount, rawPayload);
    }
}
