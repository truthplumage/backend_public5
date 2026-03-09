package com.grepp.backend5.payment.presentation.dto;


import com.grepp.backend5.payment.application.dto.PaymentCommand;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 토스 결제 완료 후 프론트에서 전달하는 파라미터.
 */
@Schema(description = "결제 승인 요청")
public record PaymentRequest(
        @Schema(description = "토스 결제 키")
        String paymentKey,
        @Schema(description = "주문 ID")
        String orderId,
        @Schema(description = "결제 금액")
        Long amount
) {

    public PaymentCommand toCommand() {
        return new PaymentCommand(paymentKey, orderId, amount);
    }
}
