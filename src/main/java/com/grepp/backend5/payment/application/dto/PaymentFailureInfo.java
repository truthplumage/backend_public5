package com.grepp.backend5.payment.application.dto;

import com.grepp.backend5.payment.domain.model.PaymentFailure;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 결제 실패 응답 DTO.
 */
@Schema(description = "결제 실패 응답")
public record PaymentFailureInfo(
        @Schema(description = "결제 실패 ID")
        UUID id,
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
        @Schema(description = "실패 기록 시각")
        LocalDateTime createdAt
) {

    public static PaymentFailureInfo from(PaymentFailure failure) {
        return new PaymentFailureInfo(
                failure.getId(),
                failure.getOrderId(),
                failure.getPaymentKey(),
                failure.getErrorCode(),
                failure.getErrorMessage(),
                failure.getAmount(),
                failure.getCreatedAt()
        );
    }
}
